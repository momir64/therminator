from mutagen import File as MutagenFile
import os


def safe_path(base: str, user_path: str | None) -> str:
    if user_path is None: return base
    abs_path = os.path.abspath(os.path.join(base, user_path.lstrip("/\\")))
    return abs_path if abs_path.startswith(os.path.abspath(base) + os.sep) else base


def valid_tracks(root: str, tracks: list) -> list:
    result = []
    for track in tracks:
        full_path = os.path.realpath(os.path.join(root, track.lstrip(os.sep)))
        if os.path.commonpath([root, full_path]) == root and os.path.isfile(full_path):
            result.append(track)
    return result


def scan(root):
    root, result, id = os.fspath(root), [], 0
    if isinstance(root, bytes): root = root.decode()
    for current, folders, files in os.walk(root):
        current = current.decode() if isinstance(current, bytes) else current
        item_path = os.path.relpath(current, root)
        item_path = "/" if item_path == "." else "/" + item_path.replace("\\", "/") + "/"
        items = [(folder, "FOLDER") for folder in folders] + [(file, "FILE") for file in files]
        for item_name, item_type in items:
            item_name = item_name.decode() if isinstance(item_name, bytes) else item_name
            item_info = {"id": id, "name": item_name, "type": item_type, "path": item_path}
            if item_type == "FILE" and item_name.lower().endswith((".mp3", ".flac", ".wav", ".m4a")):
                try:
                    audio = MutagenFile(os.path.join(current, item_name), easy=True)
                    item_info.update({
                        "title": audio.get("title", [item_name])[0] if audio else item_name,
                        "artist": audio.get("artist", ["Unknown"])[0] if audio else "Unknown",
                        "duration": int(audio.info.length) if audio and audio.info else 0
                    })
                except Exception:
                    item_info.update({"title": item_name, "artist": "Unknown", "duration": "0"})
            result.append(item_info)
            id += 1
    return result
