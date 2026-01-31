from camera import ThermalCamera
from audio import AudioPlayer
import RPi.GPIO as GPIO
import asyncio
import time

_BUTTON_PIN = 16
_BOUNCE_TIME = 50
_TEST_TIMEOUT = 10
_BUTTON_SLEEP_TIME = 1.5
_test_lock = asyncio.Lock()
_camera_lock = asyncio.Lock()


async def test(player: AudioPlayer, camera: ThermalCamera, threshold: dict):
    if _test_lock.locked():
        print("Test is already running, skipping...")
        return

    GPIO.setup(_BUTTON_PIN, GPIO.IN, pull_up_down=GPIO.PUD_UP)

    async with _test_lock:
        test_finished_event = asyncio.Event()
        loop = asyncio.get_running_loop()

        await player.play("files/test.mp3")

        async def camera_task():
            async with _camera_lock:
                await asyncio.sleep(_BUTTON_SLEEP_TIME)
                camera_queue = camera.get_queue()
                frames = 0

                start = time.time()
                while frames < threshold["frames"] and time.time() - start < _TEST_TIMEOUT:
                    values = await camera_queue.get()
                    count = sum(1 for row in values for value in row if value >= threshold["temperature"])
                    frames = frames + 1 if count >= threshold["pixels"] else 0

                if frames >= threshold["frames"]:
                    await player.play("files/test.mp3")
                else:
                    GPIO.remove_event_detect(_BUTTON_PIN)
                    loop.call_soon_threadsafe(test_finished_event.set)

        def pressed(_):
            if _camera_lock.locked(): return
            loop.call_soon_threadsafe(asyncio.create_task, player.stop())
            loop.call_soon_threadsafe(asyncio.create_task, camera_task())

        GPIO.add_event_detect(_BUTTON_PIN, GPIO.FALLING, callback=pressed, bouncetime=_BOUNCE_TIME)

        await test_finished_event.wait()
        print("Test finished")
