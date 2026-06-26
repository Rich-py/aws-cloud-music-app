from decimal import Decimal
import boto3
from boto3.dynamodb.conditions import Key
from flask import Flask, request, jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

dynamodb = boto3.resource("dynamodb", region_name="us-east-1")

login_table = dynamodb.Table("login")
music_table = dynamodb.Table("Music")
sub_table = dynamodb.Table("subscriptions")


def clean_decimal(obj):
    if isinstance(obj, list):
        return [clean_decimal(i) for i in obj]
    if isinstance(obj, dict):
        return {k: clean_decimal(v) for k, v in obj.items()}
    if isinstance(obj, Decimal):
        return int(obj) if obj % 1 == 0 else float(obj)
    return obj


def get_song_key(song):
    return (
        song.get("song_key")
        or song.get("title_year_album")
        or f"{song.get('title')}#{song.get('year')}#{song.get('album')}"
    )


@app.route("/", methods=["GET"])
def home():
    return jsonify({"message": "ECS backend running"})


@app.route("/login", methods=["POST"])
def login():
    body = request.get_json() or {}
    email = body.get("email")
    password = body.get("password")

    if not email or not password:
        return jsonify({"success": False, "message": "email or password is invalid"}), 400

    user = login_table.get_item(Key={"email": email}).get("Item")

    if not user or user.get("password") != password:
        return jsonify({"success": False, "message": "email or password is invalid"}), 401

    return jsonify({
        "success": True,
        "user": {
            "email": user.get("email"),
            "user_name": user.get("user_name") or user.get("username") or user.get("name") or email
        }
    })


@app.route("/register", methods=["POST"])
def register():
    body = request.get_json() or {}
    email = body.get("email")
    user_name = body.get("user_name")
    password = body.get("password")

    if not email or not user_name or not password:
        return jsonify({"success": False, "message": "Please complete all fields"}), 400

    existing = login_table.get_item(Key={"email": email}).get("Item")

    if existing:
        return jsonify({"success": False, "message": "The email already exists"}), 409

    login_table.put_item(Item={
        "email": email,
        "user_name": user_name,
        "password": password
    })

    return jsonify({"success": True}), 201


@app.route("/songs", methods=["GET"])
def query_songs():
    title = (request.args.get("title") or "").strip()
    artist = (request.args.get("artist") or "").strip()
    year = (request.args.get("year") or "").strip()
    album = (request.args.get("album") or "").strip()

    if not title and not artist and not year and not album:
        return jsonify({"message": "At least one field must be completed"}), 400

    items = []

    if artist:
        try:
            key_expr = Key("artist").eq(artist)
            if year:
                key_expr = key_expr & Key("year").eq(int(year))

            result = music_table.query(
                IndexName="artist-year-index",
                KeyConditionExpression=key_expr
            )
            items = result.get("Items", [])
        except Exception as e:
            print("GSI query failed:", str(e))
            items = []

    elif title:
        try:
            result = music_table.query(
                KeyConditionExpression=Key("title").eq(title)
            )
            items = result.get("Items", [])
        except Exception as e:
            print("Title query failed:", str(e))
            items = []

    if not items:
        result = music_table.scan()
        items = result.get("Items", [])

    filtered = []

    for song in items:
        song_title = str(song.get("title", "")).lower()
        song_artist = str(song.get("artist", "")).lower()
        song_year = str(song.get("year", "")).lower()
        song_album = str(song.get("album", "")).lower()

        if title and title.lower() not in song_title:
            continue
        if artist and artist.lower() not in song_artist:
            continue
        if year and year.lower() != song_year:
            continue
        if album and album.lower() not in song_album:
            continue

        song["song_key"] = get_song_key(song)
        filtered.append(song)

    filtered = clean_decimal(filtered)

    if not filtered:
        return jsonify({
            "items": [],
            "message": "No result is retrieved. Please query again"
        })

    return jsonify({"items": filtered})


@app.route("/subscriptions", methods=["GET"])
def get_subscriptions():
    email = request.args.get("email")

    if not email:
        return jsonify({"items": []})

    result = sub_table.query(
        KeyConditionExpression=Key("email").eq(email)
    )

    return jsonify({"items": clean_decimal(result.get("Items", []))})


@app.route("/subscriptions", methods=["POST"])
def add_subscription():
    body = request.get_json() or {}

    email = body.get("email")
    song = body.get("song") or {}

    if not email or not song:
        return jsonify({"success": False, "message": "Missing email or song"}), 400

    song_key = get_song_key(song)

    if not song_key:
        return jsonify({"success": False, "message": "Missing song key"}), 400

    sub_table.put_item(Item={
        "email": email,
        "song_key": song_key,
        "title": song.get("title", ""),
        "artist": song.get("artist", ""),
        "year": song.get("year", ""),
        "album": song.get("album", ""),
        "image_url": song.get("image_url", "")
    })

    return jsonify({"success": True}), 201


@app.route("/subscriptions", methods=["DELETE"])
def remove_subscription():
    email = request.args.get("email")
    song_key = request.args.get("song_key")

    if not email or not song_key:
        return jsonify({"success": False, "message": "Missing email or song_key"}), 400

    sub_table.delete_item(
        Key={
            "email": email,
            "song_key": song_key
        }
    )

    return jsonify({"success": True})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=80)