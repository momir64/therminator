from sanic.exceptions import Unauthorized, WebsocketClosed
from sanic.response import empty, json as json_response
from sanic import Sanic, Request, Websocket
from files import safe_path, valid_tracks
from battery import BatterySensor
from camera import ThermalCamera
from sanic.request import File
from audio import AudioPlayer
from sanic_cors import CORS
import asyncio
import base64
import random
import files
import httpx
import json
import os

WORKING_DIR = "/root/Desktop/therminator/server"
ALARMS_FILE = f"{WORKING_DIR}/alarms.json"
CONFIG_FILE = f"{WORKING_DIR}/config.json"
app = Sanic("Therminator")
CORS(app, resources={r"/*": {"origins": "*"}})


@app.listener("before_server_start")
async def config(server: Sanic):
    server.ctx.battery = BatterySensor()
    with open(CONFIG_FILE) as file:
        data = json.load(file)
        server.ctx.password = data["password"]
        server.ctx.google_key = data["key"]
        server.ctx.config = data["config"]
        camera = data["config"]["camera"]
        server.ctx.files_root = data["config"]["files"]["root"]
        server.ctx.camera = ThermalCamera(camera["mac"], camera["resolution"], camera["framerate"])
        server.ctx.player = AudioPlayer(data["config"]["speaker"]["mac"])
    with open(ALARMS_FILE) as file:
        server.ctx.alarms = json.load(file)


@app.middleware("request")
async def auth_middleware(request: Request):
    if request.headers.get("upgrade", "").lower() == "websocket":
        return
    header = request.headers.get("auth")
    if not header:
        raise Unauthorized("Missing authorization header")
    try:
        decoded = base64.b64decode(header).decode()
    except Exception:
        raise Unauthorized("Invalid token")
    if decoded != app.ctx.password:
        raise Unauthorized("Forbidden")


async def authenticate_ws(ws: Websocket, timeout: float = 5) -> bool:
    try:
        token = await asyncio.wait_for(ws.recv(), timeout=timeout)
        if isinstance(token, bytes): token = token.decode()
        if token is None or base64.b64decode(token).decode() != app.ctx.password:
            raise Exception("Forbidden")
    except Exception:
        await ws.close()
        return False
    return True


@app.get("/")
async def ping(request: Request):
    return empty()


@app.get("/settings/camera")
async def get_camera_settings(request: Request):
    return json_response({
        "resolution": app.ctx.config["camera"]["resolution"],
        "framerate": app.ctx.config["camera"]["framerate"],
        "threshold": app.ctx.config["camera"]["threshold"]
    })


@app.post("/settings/camera")
async def update_camera_settings(request: Request):
    body = request.json
    resolution, framerate = int(body["resolution"]), int(body["framerate"])
    await app.ctx.camera.update_settings(resolution, framerate)
    app.ctx.config["camera"]["resolution"] = resolution
    app.ctx.config["camera"]["framerate"] = framerate
    app.ctx.config["camera"]["threshold"] = body["threshold"]

    with open(CONFIG_FILE, "w") as file:
        json.dump({
            "password": app.ctx.password,
            "key": app.ctx.google_key,
            "config": app.ctx.config,
        }, file, indent=4)

    return empty()


@app.websocket("/camera")
async def camera_ws(request: Request, ws: Websocket):
    if not await authenticate_ws(ws): return
    queue = app.ctx.camera.get_queue()
    try:
        while True:
            values = await queue.get()
            await ws.send(json.dumps(values))
    except WebsocketClosed:
        pass


@app.websocket("/battery")
async def battery_ws(request: Request, ws: Websocket):
    if not await authenticate_ws(ws): return
    try:
        while True:
            is_charging = app.ctx.battery.is_charging()
            percentage = app.ctx.battery.get_percentage()
            await ws.send(f"{is_charging},{percentage}".lower())
            await asyncio.sleep(3)
    except WebsocketClosed:
        pass


@app.get("/files")
async def list_files(request: Request):
    return json_response(files.scan(app.ctx.files_root))


@app.post("/files/folder")
async def create_folder(request: Request):
    dir_path = safe_path(app.ctx.files_root, request.json.get("path", ""))
    try:
        os.mkdir(dir_path)
    except FileExistsError:
        return json_response({"error": "Directory already exists"}, status=400)
    except FileNotFoundError:
        return json_response({"error": "Parent folder does not exist"}, status=400)
    except Exception as e:
        return json_response({"error": str(e)}, status=400)
    return empty()


@app.post("/files/track")
async def upload_tracks(request: Request):
    if not request.files or "tracks" not in request.files:
        return json_response({"error": "No tracks uploaded"}, status=400)
    target_dir = safe_path(app.ctx.files_root, request.form.get("path", ""))
    for track in request.files.getlist("tracks"):
        if not isinstance(track, File): continue
        try:
            name, ext = os.path.splitext(track.name)
            filepath, counter = os.path.join(target_dir, track.name), 2
            while os.path.exists(filepath):
                filepath = os.path.join(target_dir, f"{name}_{counter}{ext}")
                counter += 1
            with open(filepath, "wb") as file:
                file.write(track.body)
        except Exception as e:
            return json_response({"error": f"Failed to save {track.name}: {e}"}, status=400)
    return empty()


@app.delete("/files")
async def delete_files(request: Request):
    paths = request.json
    if not paths: return json_response({"error": "No paths provided"}, status=400)
    for path in paths:
        target = safe_path(app.ctx.files_root, path)
        try:
            if os.path.isfile(target):
                os.remove(target)
            elif os.path.isdir(target):
                import shutil
                shutil.rmtree(target)
        except FileNotFoundError:
            return json_response({"error": f"Path not found: {path}"}, status=400)
        except Exception as e:
            return json_response({"error": f"Failed to delete {path}: {e}"}, status=400)
    return empty()


@app.get("/alarms")
async def get_alarms(request: Request):
    return json_response(app.ctx.alarms)


@app.post("/alarms")
async def update_alarms(request: Request):
    new_alarm = request.json
    new_alarm["tracks"] = valid_tracks(app.ctx.sound_test.root_path, new_alarm.get("tracks", []))
    idx = next((index for index, alarm in enumerate(app.ctx.alarms) if alarm["id"] == new_alarm.get("id")), None)
    if idx is not None:
        app.ctx.alarms[idx] = new_alarm
    else:
        new_alarm["id"] = max((alarm["id"] + 1 for alarm in app.ctx.alarms), default=0)
        app.ctx.alarms.append(new_alarm)
    with open(ALARMS_FILE, "w") as file:
        json.dump(app.ctx.alarms, file, indent=4)
    return empty()


@app.delete("/alarms")
async def delete_alarms(request: Request):
    alarm_id = request.json["id"]
    app.ctx.alarms = [alarm for alarm in app.ctx.alarms if alarm["id"] != alarm_id]
    with open(ALARMS_FILE, "w") as file:
        json.dump(app.ctx.alarms, file, indent=4)
    return empty()


@app.post("/alarms/test")
async def test_alarm(request: Request):
    alarm = request.json
    if await app.ctx.player.is_playing():
        await app.ctx.player.stop()
        return empty()
    default = app.ctx.config["files"]["default"]
    alarm["tracks"] = valid_tracks(app.ctx.files_root, alarm.get("tracks", []))
    track = app.ctx.files_root + random.choice(alarm["tracks"]) if alarm["tracks"] else default
    await app.ctx.player.play(track, alarm["volume"], alarm["speaker"] == "REMOTE")
    return empty()


@app.get("/weather/geocode", name="geocode_no_param")
@app.get("/weather/geocode/<location>", name="geocode_with_param")
async def geocode_location(request: Request, location: str = None):
    if location is None: return json_response({"location": ""})
    url = f"https://maps.googleapis.com/maps/api/geocode/json?address={location}&key={app.ctx.google_key}"
    async with httpx.AsyncClient() as client:
        response = (await client.get(url)).json()
    return json_response({
        "location": response["results"][0]["formatted_address"],
        "latitude": response["results"][0]["geometry"]["location"]["lat"],
        "longitude": response["results"][0]["geometry"]["location"]["lng"],
    })


@app.get("/weather")
async def get_location(request: Request):
    return json_response(app.ctx.config["weather"])


@app.post("/weather")
async def update_location(request: Request):
    body = request.json
    app.ctx.config["weather"]["location"] = body["location"] if "location" in body else ""
    for key in ["latitude", "longitude"]:
        if key in body:
            app.ctx.config["weather"][key] = body[key]
        else:
            app.ctx.config["weather"].pop(key, None)
    with open(CONFIG_FILE, "w") as file:
        json.dump({
            "password": app.ctx.password,
            "key": app.ctx.google_key,
            "config": app.ctx.config,
        }, file, indent=4)
    return empty()


@app.get("/display")
async def get_display(request: Request):
    return json_response(app.ctx.config["display"])


@app.post("/display")
async def update_display(request: Request):
    body = request.json
    app.ctx.config["display"]["brightness"] = body["brightness"]
    with open(CONFIG_FILE, "w") as file:
        json.dump({
            "password": app.ctx.password,
            "key": app.ctx.google_key,
            "config": app.ctx.config,
        }, file, indent=4)
    return empty()


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=200, debug=True, dev=True)
