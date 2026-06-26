import json
import boto3
import hashlib
from boto3.dynamodb.conditions import Key
from decimal import Decimal

dynamodb = boto3.resource("dynamodb", region_name="us-east-1")

login_table = dynamodb.Table("login")
music_table = dynamodb.Table("Music")
sub_table = dynamodb.Table("subscriptions")
IMAGE_BUCKET = "a1-24-music-images-2026"
IMAGE_BASE_URL = f"https://{IMAGE_BUCKET}.s3.amazonaws.com/artist-images/"


def json_default(obj):
    if isinstance(obj, Decimal):
        return int(obj) if obj % 1 == 0 else float(obj)
    raise TypeError


def response(status, body):
    return {
        "statusCode": status,
        "headers": {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "GET,POST,DELETE,OPTIONS",
            "Access-Control-Allow-Headers": "Content-Type"
        },
        "body": json.dumps(body, default=json_default)
    }


def lambda_handler(event, context):
    method = event.get("httpMethod", "")
    path = event.get("resource") or event.get("path") or ""

    if method == "POST" and path == "/login":
        return login(event)

    if method == "POST" and path == "/register":
        return register(event)

    if method == "GET" and path == "/songs":
        return query_songs(event)

    if method == "GET" and path == "/subscriptions":
        return get_subscriptions(event)

    if method == "POST" and path == "/subscriptions":
        return add_subscription(event)

    if method == "DELETE" and path == "/subscriptions":
        return remove_subscription(event)

    if method == "OPTIONS":
        return response(200, {"message": "CORS OK"})

    return response(404, {"message": "Not found", "method": method, "path": path})


def login(event):
    body = json.loads(event.get("body") or "{}")
    email = body.get("email")
    password = body.get("password")

    user = login_table.get_item(Key={"email": email}).get("Item")

    if not user or user.get("password") != password:
        return response(401, {"success": False, "message": "email or password is invalid"})

    return response(200, {
        "success": True,
        "user": {
            "email": user["email"],
            "user_name": user["user_name"]
        }
    })


def register(event):
    body = json.loads(event.get("body") or "{}")
    email = body.get("email")
    user_name = body.get("user_name")
    password = body.get("password")

    existing = login_table.get_item(Key={"email": email}).get("Item")

    if existing:
        return response(409, {"success": False, "message": "The email already exists"})

    login_table.put_item(Item={
        "email": email,
        "user_name": user_name,
        "password": password
    })

    return response(201, {"success": True})


def generate_s3_image_url(original_url):
    if not original_url:
        return ""

    if original_url.startswith(IMAGE_BASE_URL):
        return original_url

    hashed_name = hashlib.sha256(original_url.encode("utf-8")).hexdigest() + ".jpg"
    return IMAGE_BASE_URL + hashed_name


def attach_s3_image_urls(items):
    for item in items:
        original_url = item.get("image_url", "")
        item["image_url"] = generate_s3_image_url(original_url)

    return items


def query_songs(event):
    params = event.get("queryStringParameters") or {}

    title = (params.get("title") or "").strip()
    artist = (params.get("artist") or "").strip()
    year = (params.get("year") or "").strip()
    album = (params.get("album") or "").strip()

    if not title and not artist and not year and not album:
        return response(400, {"message": "At least one field must be completed"})

    
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

        filtered.append(song)

    if not filtered:
        return response(200, {
            "items": [],
            "message": "No result is retrieved. Please query again"
        })

    filtered = attach_s3_image_urls(filtered)
    return response(200, {"items": filtered})



def get_subscriptions(event):
    params = event.get("queryStringParameters") or {}
    email = params.get("email")

    result = sub_table.query(
        KeyConditionExpression=Key("email").eq(email)
    )

    items = attach_s3_image_urls(result.get("Items", []))
    return response(200, {"items": items})


def add_subscription(event):
    body = json.loads(event.get("body") or "{}")

    email = body.get("email")
    song = body.get("song")

    sub_table.put_item(Item={
        "email": email,
        "song_key": song["song_key"],
        "title": song["title"],
        "artist": song["artist"],
        "year": song["year"],
        "album": song["album"],
        "image_url": song.get("image_url", "")
    })

    return response(201, {"success": True})


def remove_subscription(event):
    params = event.get("queryStringParameters") or {}

    email = params.get("email")
    song_key = params.get("song_key")

    sub_table.delete_item(
        Key={
            "email": email,
            "song_key": song_key
        }
    )

    return response(200, {"success": True})