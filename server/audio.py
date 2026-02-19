from gpiozero import DigitalOutputDevice
import asyncio
import shlex

class AudioPlayer:
    _DEVNULL = asyncio.subprocess.DEVNULL
    _PIPE = asyncio.subprocess.PIPE
    _BLUETOOTH_DEVICE = "bluealsa"
    _LOCAL_DEVICE = "hw:0,0"
    _MAX_AMPLITUDE = 2 ** 15
    _MUTE_PIN = 25

    def __init__(self, address):
        self.process = None
        self.address = address
        self._mute_pin = DigitalOutputDevice(self._MUTE_PIN, initial_value=False)
        self._bluetooth_task = asyncio.create_task(self._bluetooth_connect_service())

    async def _bluetooth_connect_service(self):
        while True:
            try:
                cmd = f"bluetoothctl connect {self.address}"
                proc = await asyncio.create_subprocess_shell(cmd, stdout=self._PIPE, stderr=self._PIPE)
                stdout, _ = await proc.communicate()
                if b"Connection successful" not in stdout:
                    raise Exception(stdout.decode())
            except Exception as e:
                print(f"Bluetooth connection error: {e}")
            await asyncio.sleep(5)

    async def _play_on_device(self, device, filename, factor):
        if device == self._LOCAL_DEVICE: self._mute_pin.on()
        cmd = f"mpg123 --loop -1 -f {factor} -o alsa -a {device} {shlex.quote(filename)}"
        self.process = await asyncio.create_subprocess_shell(cmd, stdin=self._DEVNULL, stdout=self._PIPE, stderr=self._PIPE)

    async def play(self, filename: str, volume: int = 75, bluetooth: bool = True):
        await self.stop()
        factor = max(1, min(self._MAX_AMPLITUDE, int(self._MAX_AMPLITUDE * (volume / 100) ** 2)))
        device = self._BLUETOOTH_DEVICE if bluetooth else self._LOCAL_DEVICE
        if not bluetooth: factor = int(factor * 0.2)
        await self._play_on_device(device, filename, factor)
        if bluetooth:
            asyncio.create_task(self._handle_bluetooth_fallback(filename, factor))

    async def _handle_bluetooth_fallback(self, filename, factor):
        if not self.process:
            return
        return_code = await self.process.wait()
        print(f"Bluetooth fallback return code: {return_code}")
        if return_code != 0 and return_code != -15:
            print("Bluetooth failed, falling back to local speakers.")
            await self._play_on_device(self._LOCAL_DEVICE, filename, factor)

    async def stop(self):
        self._mute_pin.off()
        if self.process and self.process.returncode is None:
            try:
                self.process.terminate()
                await asyncio.wait_for(self.process.wait(), timeout=1.0)
            except asyncio.TimeoutError:
                self.process.kill()
            except ProcessLookupError:
                pass
            self.process = None

        proc = await asyncio.create_subprocess_exec("pkill", "-f", "mpg123", stdout=self._DEVNULL, stderr=self._DEVNULL)
        await proc.wait()

    async def is_playing(self):
        proc = await asyncio.create_subprocess_exec("pgrep", "-f", "mpg123", stdout=self._DEVNULL, stderr=self._DEVNULL)
        return await proc.wait() == 0

    async def cleanup(self):
        await self.stop()
        if self._bluetooth_task:
            self._bluetooth_task.cancel()
            try:
                await self._bluetooth_task
            except asyncio.CancelledError:
                pass
