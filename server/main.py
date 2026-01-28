from sanic.exceptions import Unauthorized, WebsocketClosed
from sanic.response import empty, json as json_response
from sanic import Sanic, Request, Websocket
from battery import BatterySensor
from camera import ThermalCamera
import asyncio
import base64
import json

app = Sanic("SleepTherminator")
CONFIG_FILE = "config.json"


@app.listener("before_server_start")
async def config(server: Sanic):
    server.ctx.battery = BatterySensor()
    with open(CONFIG_FILE) as file:
        data = json.load(file)
        server.ctx.password = data["password"]
        server.ctx.config = data["config"]
        camera = data["config"]["camera"]
        server.ctx.camera = ThermalCamera(camera["mac"], camera["resolution"], camera["framerate"])


@app.middleware("request")
async def auth_middleware(request: Request):
    header = request.headers.get("auth")
    if not header:
        raise Unauthorized("Missing authorization header")
    try:
        decoded = base64.b64decode(header).decode()
    except Exception:
        raise Unauthorized("Invalid token")
    if decoded != app.ctx.password:
        raise Unauthorized("Forbidden")


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

    with open(CONFIG_FILE, "w") as file:
        json.dump({
            "password": app.ctx.password,
            "config": app.ctx.config,
        }, file, indent=4)

    return empty()


@app.websocket("/camera")
async def camera_ws(request: Request, ws: Websocket):
    queue = app.ctx.camera.get_queue()
    try:
        while True:
            values = await queue.get()
            await ws.send(json.dumps(values))
    except WebsocketClosed:
        pass


@app.websocket("/battery")
async def battery_ws(request: Request, ws: Websocket):
    try:
        while True:
            await ws.send(str(app.ctx.battery.get_percentage()))
            await asyncio.sleep(5)
    except WebsocketClosed:
        pass


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=200, debug=True, dev=True)
