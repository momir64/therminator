from bleak import BleakClient, BleakError
import numpy as np
import asyncio
import json
import os

WORKING_DIR = "/root/Desktop/therminator/server"
CONFIG_FILE = f"{WORKING_DIR}/config.json"


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

    def __init__(self, address, resolution, framerate, socket_path):
        self._frames = {}
        self._address = address
        self._framerate = framerate
        self._resolution = resolution
        self._socket_path = socket_path
        self._loop = asyncio.get_running_loop()
        self._clients: dict[asyncio.StreamWriter, asyncio.Queue] = {}

    async def start(self):
        if os.path.exists(self._socket_path):
            os.remove(self._socket_path)
        server = await asyncio.start_unix_server(self._handle_client, path=self._socket_path)
        print(f"Listening on {self._socket_path}")
        self._loop.create_task(self._ble_loop())
        async with server:
            await server.serve_forever()

    async def _handle_client(self, reader: asyncio.StreamReader, writer: asyncio.StreamWriter):
        queue: asyncio.Queue = asyncio.Queue(maxsize=10)
        self._clients[writer] = queue
        print(f"Client connected ({len(self._clients)} total)")

        async def write_frames():
            try:
                while True:
                    frame_line = await queue.get()
                    writer.write(frame_line.encode())
                    await writer.drain()
            except (ConnectionResetError, BrokenPipeError, ConnectionError):
                pass

        async def read_commands():
            try:
                while True:
                    message = await reader.readline()
                    if not message: break
                    try:
                        command = json.loads(message.strip())
                        if command.get("type") == "settings":
                            await self.update_settings(command["resolution"], command["framerate"])
                    except Exception as ex:
                        print(f"Bad command from client: {ex}")
            except (ConnectionResetError, BrokenPipeError):
                pass

        write_task = asyncio.create_task(write_frames())
        try:
            await read_commands()
        finally:
            write_task.cancel()
            self._clients.pop(writer, None)
            writer.close()
            print(f"Client disconnected ({len(self._clients)} total)")

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

            frame_line = json.dumps(values) + "\n"
            for writer, queue in list(self._clients.items()):
                try:
                    queue.put_nowait(frame_line)
                except asyncio.QueueFull:
                    _ = queue.get_nowait()
                    queue.put_nowait(frame_line)

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


async def main():
    with open(CONFIG_FILE) as file:
        config = json.load(file)["config"]["camera"]
    camera = ThermalCamera(config["mac"], config["resolution"], config["framerate"], config["socket"])
    await camera.start()


if __name__ == "__main__":
    asyncio.run(main())
