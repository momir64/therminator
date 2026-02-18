from audio import AudioPlayer
from pathlib import Path
import random


class SoundTest:
    _BUTTON_PIN = 16
    _BOUNCE_TIME = 50

    def __init__(self, player: AudioPlayer, root_path: str, default_track: str):
        self.default_track = default_track
        self.root_path = root_path
        self.player = player
        self._task = None

    async def test(self, alarm: dict):
        if await self.player.is_playing():
            await self.player.stop()
            return

        if alarm["tracks"]:
            track = self.root_path + random.choice(alarm["tracks"])
        else:
            track = str(Path(self.root_path).parent) + self.default_track

        await self.player.play(track, alarm["volume"], alarm["speaker"] == "REMOTE")
