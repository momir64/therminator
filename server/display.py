from watchdog.events import FileSystemEventHandler
from watchdog.observers import Observer
from battery import BatterySensor
from PIL import Image, ImageTk
from tkinter import font
import tkinter as tk
import threading
import cairosvg
import asyncio
import httpx
import queue
import json
import time
import io
import os

WEATHER_URL = "https://weather.googleapis.com/v1/currentConditions:lookup"
WORKING_DIR = "/root/Desktop/therminator/server"
CONFIG_FILE = f"{WORKING_DIR}/config.json"
ICONS_DIR = f"{WORKING_DIR}/icons"


class ConfigHandler(FileSystemEventHandler):
    def __init__(self, display):
        self.display = display

    def on_closed(self, event):
        if event.src_path.endswith(CONFIG_FILE):
            self.display.task_queue.put(self.display.load_config)


class Display:
    def __init__(self):
        self.root = tk.Tk()
        self.root.configure(bg="black")
        self.root.geometry(f"{self.root.winfo_screenwidth()}x{self.root.winfo_screenheight() - 64}+0+61")
        self.loop = asyncio.new_event_loop()
        self.task_queue = queue.Queue()
        self.battery = BatterySensor()

        self.time_font = font.Font(family="Jura", size=186, weight="bold")
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

        self.battery_label = tk.Label(self.battery_frame, font=self.battery_font, fg="white", bg="black")
        self.battery_label.pack(side="right")

        self.weather_icon_label = tk.Label(self.info_frame, bg="black")
        self.weather_icon_label.pack(anchor="center", pady=(32, 0), fill="both", expand=True)
        self.weather_icon_raw = None

        self.weather_label = tk.Label(self.info_frame, font=self.weather_font, fg="white", bg="black")
        self.weather_label.pack(anchor="s", pady=(0, 32))

        self.load_config()
        self.start_weather_loop()
        self.start_watchdog()
        self.update()

    def load_config(self):
        with open(CONFIG_FILE) as file:
            config = json.load(file)
            weather = config["config"]["weather"]
            brightness = config["config"]["display"]["brightness"]
            self.longitude = weather.get("longitude")
            self.latitude = weather.get("latitude")
            self.city = weather.get("location")
            self.google_key = config.get("key")
            self.brightness = brightness / 100
        self.apply_brightness()
        self.update_weather()

    def apply_brightness(self):
        value = int(self.brightness * 255)
        color = "#" + f"{value:02x}" * 3
        for label in (self.time_label, self.weather_label, self.battery_label):
            label.config(fg=color)
        if self.weather_icon_raw:
            self.weather_icon_label.config(image=ImageTk.PhotoImage(self.with_brightness(self.weather_icon_raw)))

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
        self.battery_icon_label.config(image=ImageTk.PhotoImage(self.with_brightness(Image.open(io.BytesIO(icon)))))
        return f"{int(percent)}%" if percent is not None else ""

    async def fetch_weather(self):
        if not self.google_key: return "N/A"
        url = f"{WEATHER_URL}?key={self.google_key}&location.latitude={self.latitude}&location.longitude={self.longitude}"
        try:
            async with httpx.AsyncClient(timeout=5) as client:
                data = (await client.get(url)).json()
                temperature = data.get("temperature", {}).get("degrees")
                response = await client.get(f"{data.get("weatherCondition", {}).get("iconBaseUri")}_dark.png")
                self.weather_icon_raw = Image.open(io.BytesIO(response.content))
                self.weather_icon_label.config(image=ImageTk.PhotoImage(self.with_brightness(self.weather_icon_raw)))
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
        self.time_label.config(text=time.strftime("%H:%M"))
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
        while True:
            try:
                self.root.update()
            except tk.TclError:
                break
            await asyncio.sleep(0.05)


if __name__ == "__main__":
    Display().run()
