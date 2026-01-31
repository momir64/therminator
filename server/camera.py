from bleak import BleakClient, BleakError
import numpy as np
import asyncio
import weakref


class ThermalCamera:
    _SETTINGS_UUID = "173f51fe-0ca9-4aac-873c-ac2811209099"
    _FRAME_UUID = "705b66b4-8f43-440e-96fd-e071ab46d471"
    _PING_UUID = "8f4a7e58-9c3e-4fbe-8e0d-7e7d2e8a7b1a"
    _PING_INTERVAL = 1.0
    _RECONNECT_DELAY = 2
    _MAX_FRAME_AGE = 5
    _UINT8_MAX = 2 ** 8
    _WIDTH, _HEIGHT = 32, 24
    _TOTAL_SIZE = _WIDTH * _HEIGHT

    def __init__(self, address, resolution, framerate):
        self._frames = {}
        self._address = address
        self._framerate = framerate
        self._resolution = resolution
        self._loop = asyncio.get_running_loop()
        self._loop.create_task(self._ble_loop())
        self._subscriber_queues = weakref.WeakSet()

    async def _ble_loop(self):
        while True:
            try:
                self._client = BleakClient(self._address)
                await self._client.connect()
                self._frames.clear()

                await self.update_settings(self._resolution, self._framerate)
                await self._client.start_notify(self._FRAME_UUID, self._frame_callback)

                while self._client.is_connected:
                    await self._client.write_gatt_char(self._PING_UUID, bytearray([1]))
                    await asyncio.sleep(self._PING_INTERVAL)

                raise BleakError("Device disconnected")
            except Exception as e:
                print(f"Connection lost: {e}\n Reconnecting in {self._RECONNECT_DELAY}s...")

                try:
                    await self._client.stop_notify(self._FRAME_UUID)
                except Exception as e:
                    print(f"Exception stopping notify: {e}")

                try:
                    await self._client.disconnect()
                except Exception as e:
                    print(f"Exception disconnecting: {e}")

                await asyncio.sleep(self._RECONNECT_DELAY)

    def _frame_callback(self, sender, data):
        frame_idx = data[0]
        chunk_idx = data[1]
        chunk_data = data[2:]

        self._frames.setdefault(frame_idx, {})
        self._frames[frame_idx][chunk_idx] = chunk_data
        chunks = self._frames[frame_idx]

        received_bytes = sum(len(chunk) for chunk in chunks.values())
        if received_bytes >= self._TOTAL_SIZE * 2:
            ordered = [chunks[i] for i in sorted(chunks)]
            frame_bytes = b''.join(ordered)[:self._TOTAL_SIZE * 2]
            arr16 = np.frombuffer(frame_bytes, dtype=np.uint16).reshape(self._HEIGHT, self._WIDTH)
            values = [[round(value, 3) for value in row] for row in (arr16.astype(np.float32) / 1000.0).tolist()]

            for queue in self._subscriber_queues:
                def put_frame(q, vals):
                    try:
                        q.put_nowait(vals)
                    except asyncio.QueueFull:
                        _ = q.get_nowait()
                        q.put_nowait(vals)
                self._loop.call_soon_threadsafe(put_frame, queue, values)

            del self._frames[frame_idx]

        for idx in list(self._frames):
            if frame_idx - idx > self._MAX_FRAME_AGE or (frame_idx < self._MAX_FRAME_AGE and idx > self._UINT8_MAX - self._MAX_FRAME_AGE):
                del self._frames[idx]

    async def update_settings(self, resolution, framerate):
        self._resolution = resolution
        self._framerate = framerate
        try:
            if self._client.is_connected:
                data = bytearray([framerate & 0xFF, resolution & 0xFF])
                await self._client.write_gatt_char(self._SETTINGS_UUID, data)
        except Exception as e:
            print(f"Failed to update settings: {e}")

    def get_queue(self) -> asyncio.Queue[list[list[float]]]:
        queue = asyncio.Queue(maxsize=10)
        self._subscriber_queues.add(queue)
        return queue
