from watchdog.events import FileSystemEventHandler
from watchdog.observers import Observer
from battery import BatterySensor
from PIL import Image, ImageTk
from files import valid_tracks
from audio import AudioPlayer
from gpiozero import Button
from tkinter import font
import tkinter as tk
import threading
import cairosvg
import datetime
import asyncio
import random
import httpx
import queue
import json
import time
import io
import os

WEATHER_URL = "https://weather.googleapis.com/v1/currentConditions:lookup"
WORKING_DIR = "/root/Desktop/therminator/server"
SOUND_TEST_FILE = f"{WORKING_DIR}/test.json"
CONFIG_FILE = f"{WORKING_DIR}/config.json"
ALARMS_FILE = f"{WORKING_DIR}/alarms.json"
ICONS_DIR = f"{WORKING_DIR}/icons"
NEXT_ALARM_DISPLAY_DURATION = 3200
BOUNCE_TIME = 50
BUTTON_PIN = 16


class ConfigHandler(FileSystemEventHandler):
    def __init__(self, display):
        self.display = display

    def on_closed(self, event):
        if event.src_path.endswith(CONFIG_FILE):
            self.display.task_queue.put(self.display.load_config)
        elif event.src_path.endswith(ALARMS_FILE):
            self.display.task_queue.put(self.display.load_alarms)
        elif event.src_path.endswith(SOUND_TEST_FILE):
            self.display.task_queue.put(self.display.handle_test_alarm)


class Display:
    def __init__(self):
        self.root = tk.Tk()
        self.root.configure(bg="black")
        self.root.geometry(f"{self.root.winfo_screenwidth()}x{self.root.winfo_screenheight() - 64}+0+61")
        self.loop = asyncio.new_event_loop()
        self.task_queue = queue.Queue()
        self.battery = BatterySensor()
        self.showing_next_alarm = False
        self.speaker_address = None
        self.testing_alarm = False
        self.active_alarm = None
        self.player = None
        self.alarms = []

        self.time_font = font.Font(family="Jura", size=186, weight="bold")
        self.next_alarm_font = font.Font(family="Jura", size=56, weight="bold")
        self.weather_font = font.Font(family="Jura", size=42, weight="bold")
        self.battery_font = font.Font(family="Jura", size=28)

        self.time_label = tk.Label(self.root, font=self.time_font, fg="white", bg="black")
        self.time_label.place(relx=0, rely=0, relwidth=0.84, relheight=1.0)

        self.info_frame = tk.Frame(self.root, bg="black")
        self.info_frame.place(relx=0.82, rely=0, relwidth=0.18, relheight=1.0)

        self.battery_frame = tk.Frame(self.info_frame, bg="black")
        self.battery_frame.pack(anchor="ne", pady=(16, 0), padx=(0, 16), fill="x")

        self.battery_icon_label = tk.Label(self.battery_frame, bg="black")
        self.battery_icon_label.pack(side="right")
        self.battery_icon_photo = None

        self.battery_label = tk.Label(self.battery_frame, font=self.battery_font, fg="white", bg="black")
        self.battery_label.pack(side="right")

        self.weather_icon_label = tk.Label(self.info_frame, bg="black")
        self.weather_icon_label.pack(anchor="center", pady=(32, 0), fill="both", expand=True)
        self.weather_icon_photo = None
        self.weather_icon_raw = None

        self.weather_label = tk.Label(self.info_frame, font=self.weather_font, fg="white", bg="black")
        self.weather_label.pack(anchor="s", pady=(0, 32))

        self.button = Button(BUTTON_PIN, bounce_time=BOUNCE_TIME / 1000)
        self.button.when_pressed = self.button_pressed

        self.load_alarms()
        self.load_config()
        self.start_weather_loop()
        self.start_watchdog()
        self.update()

    def button_pressed(self):
        if self.active_alarm is not None or self.testing_alarm:
            self.task_queue.put(self.dismiss_alarm)
        else:
            self.task_queue.put(self.show_next_alarm)

    def dismiss_alarm(self):
        self.active_alarm = None
        self.testing_alarm = False
        asyncio.run_coroutine_threadsafe(self.player.stop(), self.loop)
        with open(SOUND_TEST_FILE, "w") as file:
            json.dump(None, file)

    def trigger_alarm(self, alarm):
        if self.active_alarm is not None:
            asyncio.run_coroutine_threadsafe(self.player.stop(), self.loop)
        self.active_alarm = alarm
        tracks = alarm.get("tracks", [])
        track = self.files_root + random.choice(tracks) if tracks else self.default_track
        asyncio.run_coroutine_threadsafe(self.player.play(track, alarm["volume"], alarm.get("speaker") == "REMOTE"), self.loop)

    def check_alarms(self):
        now = datetime.datetime.now()
        today_alarms = [alarm for alarm in self.alarms if not alarm["days"] or now.weekday() in alarm["days"]]
        due_alarm = next((alarm for alarm in today_alarms if alarm["hours"] == now.hour and alarm["minutes"] == now.minute), None)
        if due_alarm is not None and (not self.active_alarm or self.active_alarm["id"] == due_alarm["id"]):
            self.trigger_alarm(due_alarm)  # type: ignore

    def show_next_alarm(self):
        self.showing_next_alarm = True
        next_alarm = self.get_next_alarm()
        if next_alarm is None:
            text = "No alarms set"
        else:
            days, hours, minutes = self.time_until_alarm(next_alarm)  # type: ignore
            parts = f" {days}d" * (days != 0) + f" {hours}h" * (days != 0 or hours != 0) + f" {minutes}m"
            text = f"Next alarm in\n{parts.strip()}"
        self.time_label.config(text=text, font=self.next_alarm_font)
        self.root.after(NEXT_ALARM_DISPLAY_DURATION, self.hide_next_alarm)

    def hide_next_alarm(self):
        self.showing_next_alarm = False

    def get_next_alarm(self):
        return min(self.alarms, key=lambda alarm: self.minutes_until_alarm(alarm), default=None)

    @staticmethod
    def minutes_until_alarm(alarm):
        now = datetime.datetime.now()
        for days_ahead in range(8):
            date = now.date() + datetime.timedelta(days=days_ahead)
            candidate = datetime.datetime(date.year, date.month, date.day, alarm["hours"], alarm["minutes"])
            if candidate > now and (not alarm["days"] or date.weekday() in alarm["days"]):
                return int((candidate - now).total_seconds()) // 60
        return 0

    @staticmethod
    def time_until_alarm(alarm):
        difference = Display.minutes_until_alarm(alarm)
        return difference // (60 * 24), difference // 60 % 24, difference % 60

    def load_config(self):
        with open(CONFIG_FILE) as file:
            data = json.load(file)
            config = data["config"]
            weather = config["weather"]
            brightness = config["display"]["brightness"]
            self.speaker_address = config["speaker"]["mac"]
            self.default_track = config["files"]["default"]
            self.files_root = config["files"]["root"]
            self.longitude = weather.get("longitude")
            self.latitude = weather.get("latitude")
            self.city = weather.get("location")
            self.google_key = data["key"]
            self.brightness = brightness / 100
        self.apply_brightness()
        self.update_weather()

    def load_alarms(self):
        with open(ALARMS_FILE) as file:
            self.alarms = [alarm for alarm in json.load(file) if alarm["active"]]

    def apply_brightness(self):
        value = int(self.brightness * 255)
        color = "#" + f"{value:02x}" * 3
        for label in (self.time_label, self.weather_label, self.battery_label):
            label.config(fg=color)
        if self.weather_icon_raw:
            self.weather_icon_photo = ImageTk.PhotoImage(self.with_brightness(self.weather_icon_raw))
            self.weather_icon_label.config(image=self.weather_icon_photo)

    def with_brightness(self, image):
        channels = image.split()
        rgb = [channel.point(lambda c: int(c * self.brightness)) for channel in channels[:3]]
        return Image.merge(image.mode, (*rgb, *channels[3:]))

    @staticmethod
    def get_battery_icon_filepath(percent, charging):
        if percent is None: return "battery"
        level = next(idx for idx, threshold in enumerate([20, 30, 40, 50, 60, 75, 90, 100]) if percent <= threshold)
        return os.path.join(ICONS_DIR, f"battery_charging_{level}.svg" if charging else f"battery_{level}.svg")

    def get_battery(self):
        charging = self.battery.is_charging()
        percent = self.battery.get_percentage()
        icon = cairosvg.svg2png(url=self.get_battery_icon_filepath(percent, charging), output_height=42)
        self.battery_icon_photo = ImageTk.PhotoImage(self.with_brightness(Image.open(io.BytesIO(icon))))
        self.battery_icon_label.config(image=self.battery_icon_photo)
        return f"{int(percent)}%" if percent is not None else ""

    async def fetch_weather(self):
        if not self.google_key: return "N/A"
        url = f"{WEATHER_URL}?key={self.google_key}&location.latitude={self.latitude}&location.longitude={self.longitude}"
        try:
            async with httpx.AsyncClient(timeout=5) as client:
                data = (await client.get(url)).json()
                temperature = data.get("temperature", {}).get("degrees")
                response = await client.get(f"{data.get('weatherCondition', {}).get('iconBaseUri')}_dark.png")
                self.weather_icon_raw = Image.open(io.BytesIO(response.content))
                self.weather_icon_photo = ImageTk.PhotoImage(self.with_brightness(self.weather_icon_raw))
                self.weather_icon_label.config(image=self.weather_icon_photo)
                return f"{int(temperature)}°C" if temperature is not None else "N/A"
        except:
            return "N/A"

    def start_watchdog(self):
        observer = Observer()
        observer.schedule(ConfigHandler(self), path=os.path.dirname(os.path.abspath(CONFIG_FILE)), recursive=False)
        threading.Thread(target=observer.start, daemon=True).start()

    def update(self):
        while not self.task_queue.empty():
            self.task_queue.get()()
        self.check_alarms()
        if not self.showing_next_alarm:
            self.time_label.config(text=time.strftime("%H:%M"))
            self.time_label.config(font=self.time_font)
        self.battery_label.config(text=self.get_battery())
        self.root.after(1000, self.update)

    def update_weather(self):
        async def _update_weather_inner():
            self.weather_label.config(text=await self.fetch_weather())

        asyncio.run_coroutine_threadsafe(_update_weather_inner(), self.loop)

    def start_weather_loop(self):
        self.update_weather()
        self.root.after(60000, self.start_weather_loop)

    def run(self):
        self.loop.create_task(self.tk_loop())
        self.loop.run_forever()

    async def tk_loop(self):
        self.player = AudioPlayer(self.speaker_address)
        while True:
            try:
                self.root.update()
            except tk.TclError:
                break
            await asyncio.sleep(0.05)

    def handle_test_alarm(self):
        if self.active_alarm is not None: return
        with open(SOUND_TEST_FILE) as file:
            alarm = json.load(file)
        if alarm is None:
            self.testing_alarm = False
            asyncio.run_coroutine_threadsafe(self.player.stop(), self.loop)
        else:
            self.testing_alarm = True
            tracks = valid_tracks(self.files_root, alarm.get("tracks", []))
            track = self.files_root + random.choice(tracks) if tracks else self.default_track
            asyncio.run_coroutine_threadsafe(self.player.play(track, alarm["volume"], alarm.get("speaker") == "REMOTE"), self.loop)


if __name__ == "__main__":
    Display().run()
