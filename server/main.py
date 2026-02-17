from sanic.exceptions import Unauthorized, WebsocketClosed
from sanic.response import empty, json as json_response
from sanic import Sanic, Request, Websocket
from test import test as alarm_test
from battery import BatterySensor
from camera import ThermalCamera
from sanic.request import File
from audio import AudioPlayer
from files import safe_path
from sanic_cors import CORS
import asyncio
import base64
import files
import json
import os

CONFIG_FILE = "config.json"
app = Sanic("Therminator")
CORS(app, resources={r"/*": {"origins": "*"}})


@app.listener("before_server_start")
async def config(server: Sanic):
    server.ctx.battery = BatterySensor()
    with open(CONFIG_FILE) as file:
        data = json.load(file)
        server.ctx.password = data["password"]
        server.ctx.config = data["config"]
        camera = data["config"]["camera"]
        server.ctx.files_root = data["config"]["files"]["root"]
        server.ctx.camera = ThermalCamera(camera["mac"], camera["resolution"], camera["framerate"])
        server.ctx.player = AudioPlayer(data["config"]["speaker"]["mac"])


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
            "config": app.ctx.config,
        }, file, indent=4)

    return empty()


@app.websocket("/camera")
async def camera_ws(request: Request, ws: Websocket):
    if not await authenticate_ws(ws):
        return
    queue = app.ctx.camera.get_queue()
    try:
        while True:
            values = await queue.get()
            await ws.send(json.dumps(values))
    except WebsocketClosed:
        pass


@app.websocket("/battery")
async def battery_ws(request: Request, ws: Websocket):
    if not await authenticate_ws(ws):
        return
    try:
        while True:
            is_charging = app.ctx.battery.is_charging()
            percentage = app.ctx.battery.get_percentage()
            await ws.send(f"{is_charging},{percentage}".lower())
            await asyncio.sleep(3)
    except WebsocketClosed:
        pass


@app.get("/test")
async def test(request: Request):
    asyncio.create_task(alarm_test(app.ctx.player, app.ctx.camera, app.ctx.config["camera"]["threshold"]))
    return empty()


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


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=200, debug=True, dev=True)
