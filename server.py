from __future__ import annotations

import json
import hashlib
import re
import sqlite3
import uuid
from pathlib import Path
from typing import Any

import joblib
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from flowers_catalog_ru import (
    budget_rules,
    color_aliases,
    flowers_catalog,
    recipient_aliases,
    recipient_recommendations,
)


BASE_DIR = Path(__file__).resolve().parent
MODEL_PATH = BASE_DIR / "intent_model.pkl"
DB_PATH = BASE_DIR / "flower_ai.db"

app = FastAPI(title="Flower AI Consultant")
model = joblib.load(MODEL_PATH)
sessions: dict[str, dict[str, Any]] = {}
WRAPPING_AND_RIBBON_PRICE = 55
FLOWER_PRICE_BY_SLUG = {flower["slug"]: flower["price"] for flower in flowers_catalog}

existing_bouquets = [
    {
        "id": 1,
        "name": "Noir Lavender",
        "currency": "лей",
        "image_resource": "bouquet1",
        "colors": ["фиолетовый", "сиреневый", "белый", "темный"],
        "composition": {"lavender": 3, "eustoma": 3, "rose": 5, "gypsophila": 2},
        "best_for": ["девушка", "жена", "любимая", "подруга"],
        "events": ["свидание", "годовщина", "день рождения", "без повода"],
        "style": ["нежный", "вечерний", "романтичный", "ароматный"],
        "note": "Глубокий лавандовый букет в вечерней эстетике: мягкая эустома, выразительные розы и воздушная гипсофила создают нежный, но запоминающийся подарок. Хорош для свиданий, годовщин и случаев, когда хочется подарить что-то тонкое, ароматное и не банальное.",
    },
    {
        "id": 2,
        "name": "Crimson Eclipse",
        "currency": "лей",
        "image_resource": "bouquet2",
        "colors": ["красный", "бордовый", "марсала", "темный"],
        "composition": {"rose": 7, "protea": 1, "amaranthus": 3, "eucalyptus": 3},
        "best_for": ["жена", "любимая", "девушка", "мужчина"],
        "events": ["годовщина", "свидание", "юбилей", "день рождения"],
        "style": ["страстный", "премиальный", "драматичный", "выразительный"],
        "note": "Драматичная композиция в винно-красной гамме с протеей и амарантом. Букет выглядит смело, дорого и подходит для подарка, который должен произвести впечатление: годовщина, юбилей, признание или статусный жест.",
    },
    {
        "id": 3,
        "name": "Blue Mist",
        "currency": "лей",
        "image_resource": "bouquet3",
        "colors": ["голубой", "синий", "белый", "пастельный"],
        "composition": {"hydrangea": 2, "iris": 5, "eustoma": 3, "delphinium": 3},
        "best_for": ["мама", "девушка", "подруга", "сестра", "врач"],
        "events": ["день рождения", "благодарность", "без повода", "свадьба"],
        "style": ["нежный", "свежий", "воздушный", "спокойный"],
        "note": "Свежий голубой букет с гортензией, ирисами и эустомой. Он выглядит чисто, спокойно и воздушно, поэтому хорошо подходит для мамы, подруги, врача, благодарности или аккуратного подарка на день рождения.",
    },
    {
        "id": 4,
        "name": "Ghost Orchid",
        "currency": "лей",
        "image_resource": "bouquet4",
        "colors": ["белый", "фиолетовый", "кремовый", "зеленый"],
        "composition": {"orchid": 3, "calla": 5, "eucalyptus": 3, "eustoma": 3},
        "best_for": ["жена", "начальница", "клиент", "партнер", "невеста"],
        "events": ["юбилей", "свадьба", "годовщина", "деловой подарок"],
        "style": ["элегантный", "премиальный", "статусный", "минималистичный"],
        "note": "Элегантный бело-зеленый букет с орхидеями и каллами. Минималистичный, чистый и статусный вариант для свадьбы, юбилея, делового подарка или случая, где важны вкус и сдержанная роскошь.",
    },
    {
        "id": 5,
        "name": "Pink Reverie",
        "currency": "лей",
        "image_resource": "bouquet5_pink",
        "colors": ["розовый", "пудровый", "белый", "зеленый"],
        "composition": {"ranunculus": 7, "tulip": 7, "eucalyptus": 3},
        "best_for": ["мама", "девушка", "любимая", "подруга", "сестра"],
        "events": ["день рождения", "свидание", "без повода", "благодарность", "8 марта"],
        "style": ["нежный", "весенний", "романтичный", "пудровый"],
        "note": "Нежная розовая композиция с ранункулюсами, тюльпанами и эвкалиптом. Один и тот же букет можно выбрать в черной, пудрово-розовой или белой обертке, чтобы настроение подарка стало более драматичным, мягким или классическим.",
    },
]


def calculate_bouquet_price(composition: dict[str, int]) -> int:
    flowers_total = sum(
        FLOWER_PRICE_BY_SLUG[slug] * quantity
        for slug, quantity in composition.items()
    )
    return flowers_total + WRAPPING_AND_RIBBON_PRICE

QUESTION_SLOTS = ["recipient", "age", "event", "color", "budget"]
UNKNOWN_MARKERS = [
    "не знаю",
    "незнаю",
    "хз",
    "без понятия",
    "понятия не имею",
    "сложно сказать",
    "затрудняюсь",
    "не уверен",
    "не уверена",
    "не важно",
    "неважно",
    "без разницы",
    "все равно",
    "любой",
    "любая",
    "любое",
    "на твой вкус",
    "как лучше",
    "выбери сам",
    "выбери сама",
    "реши за меня",
]

flower_care_aliases = {
    "rose": ["роза", "розы", "розами", "розу"],
    "tulip": ["тюльпан", "тюльпаны", "тюльпанами"],
    "lily": ["лилия", "лилии", "лилиями", "лилию"],
    "chrysanthemum": ["хризантема", "хризантемы", "хризантемами"],
    "eustoma": ["эустома", "эустомы", "эустомой"],
    "peony": ["пион", "пионы", "пионами"],
    "hydrangea": ["гортензия", "гортензии", "гортензию"],
    "carnation": ["гвоздика", "гвоздики", "гвоздиками"],
    "chamomile": ["ромашка", "ромашки", "ромашками"],
    "iris": ["ирис", "ирисы", "ирисами"],
    "lavender": ["лаванда", "лаванды", "лавандой"],
    "sunflower": ["подсолнечник", "подсолнух", "подсолнухи"],
    "orchid": ["орхидея", "орхидеи", "орхидеями"],
    "gypsophila": ["гипсофила", "гипсофилы", "гипсофилой"],
    "anemone": ["анемон", "анемоны", "анемонами"],
    "ranunculus": ["ранункулюс", "ранункулюсы", "ранункулюсом"],
    "alstroemeria": ["альстромерия", "альстромерии", "альстромерией"],
    "calla": ["калла", "каллы", "каллами"],
    "freesia": ["фрезия", "фрезии", "фрезией"],
    "magnolia": ["магнолия", "магнолии", "магнолией"],
    "gerbera": ["гербера", "герберы", "герберами"],
    "hyacinth": ["гиацинт", "гиацинты", "гиацинтами"],
    "daffodil": ["нарцисс", "нарциссы", "нарциссами"],
    "delphinium": ["дельфиниум", "дельфиниумы", "дельфиниумом"],
    "matthiola": ["маттиола", "маттиолы", "маттиолой"],
    "protea": ["протея", "протеи", "протеей"],
    "amaranthus": ["амарант", "амаранты", "амарантом"],
    "eucalyptus": ["эвкалипт", "эвкалиптом"],
    "statice": ["статица", "статицы", "статицей"],
    "astilbe": ["астильба", "астильбы", "астильбой"],
}

specific_care_tips = {
    "rose": "У роз обязательно обновите срез под углом и уберите листья ниже уровня воды. Если головки начали клониться, заверните бутоны в бумагу и поставьте стебли в прохладную воду на пару часов.",
    "tulip": "Тюльпаны продолжают расти в вазе, поэтому им лучше прохладная вода и высокая узкая ваза. Не ставьте их на солнце: так они быстрее раскрываются и теряют форму.",
    "lily": "У лилий лучше убрать пыльники, чтобы пыльца не пачкала лепестки и одежду. Ставьте их в просторное место: аромат может быть насыщенным.",
    "hydrangea": "Гортензия очень любит воду. Обновите срез, можно слегка расщепить кончик стебля, а при увядании погрузить соцветие в прохладную воду на 20-30 минут.",
    "peony": "Пионы быстрее раскрываются в тепле. Если хотите сохранить их дольше, держите букет в прохладе и меняйте воду каждый день.",
    "orchid": "Орхидеи в срезке любят чистую воду и прохладу без сквозняка. Не ставьте их рядом с фруктами и батареей.",
    "gerbera": "У гербер мягкий стебель: воды в вазе должно быть немного, примерно 5-7 см. Так стебель меньше размокает и букет дольше держит форму.",
    "gypsophila": "Гипсофила хорошо стоит и красиво высыхает. Если хотите сухоцвет, достаньте ее из воды и подвесьте в сухом проветриваемом месте.",
    "lavender": "Лаванду можно держать в воде или высушить. Для сухого букета подвесьте ее вниз соцветиями в темном сухом месте.",
    "eucalyptus": "Эвкалипт стойкий и может высыхать декоративно. Меняйте воду, а когда ветви начнут подсыхать, их можно оставить как сухоцвет.",
    "statice": "Статица отлично подходит для сухоцвета. Можно поставить в вазу без воды после того, как букет начнет подсыхать.",
    "protea": "Протея стойкая и любит чистую воду. Держите ее в прохладном месте, а позже можно высушить как декоративный акцент.",
}

recipient_display = {
    "для себя": "вас",
    "девушка": "девушки",
    "любимая": "любимой",
    "жена": "жены",
    "бывшая": "бывшей",
    "мама": "мамы",
    "теща": "тещи",
    "свекровь": "свекрови",
    "бабушка": "бабушки",
    "сестра": "сестры",
    "дочь": "дочери",
    "племянница": "племянницы",
    "подруга": "подруги",
    "коллега": "коллеги",
    "начальница": "начальницы",
    "учительница": "учительницы",
    "воспитательница": "воспитательницы",
    "врач": "врача",
    "тренер": "тренера",
    "клиент": "клиента",
    "партнер": "партнера",
    "невеста": "невесты",
    "знакомая": "знакомой",
    "крестная": "крестной",
    "женщина": "женщины",
    "девочка": "девочки",
    "муж": "мужа",
    "папа": "папы",
    "брат": "брата",
    "дедушка": "дедушки",
    "парень": "парня",
    "мужчина": "мужчины",
    "любимый человек": "любимого человека",
}


class ChatRequest(BaseModel):
    text: str
    session_id: str = "default"


class AuthRequest(BaseModel):
    username: str
    password: str


class CreateChatRequest(BaseModel):
    title: str | None = None


def default_state() -> dict[str, Any]:
    return {
        "recipient": None,
        "event": None,
        "color": None,
        "budget": None,
        "age": None,
        "eye_color": None,
        "years_together": None,
        "skipped_slots": [],
        "last_bouquets": [],
        "rejected_bouquet_ids": [],
        "pending_bouquet_id": None,
        "pending_action": None,
    }


def db_connect() -> sqlite3.Connection:
    connection = sqlite3.connect(DB_PATH, timeout=15)
    connection.row_factory = sqlite3.Row
    connection.execute("PRAGMA journal_mode=WAL")
    connection.execute("PRAGMA busy_timeout=15000")
    return connection


def init_database() -> None:
    with db_connect() as connection:
        connection.execute(
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                session_id TEXT NOT NULL UNIQUE,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """
        )
        connection.execute(
            """
            CREATE TABLE IF NOT EXISTS sessions (
                session_id TEXT PRIMARY KEY,
                user_id INTEGER,
                title TEXT NOT NULL DEFAULT 'Новый чат',
                state_json TEXT NOT NULL,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """
        )
        connection.execute(
            """
            CREATE TABLE IF NOT EXISTS messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id TEXT NOT NULL,
                role TEXT NOT NULL,
                text TEXT NOT NULL,
                intent TEXT,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """
        )
        connection.execute(
            """
            CREATE TABLE IF NOT EXISTS flowers (
                slug TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                price INTEGER NOT NULL,
                currency TEXT NOT NULL,
                colors_json TEXT NOT NULL,
                meaning_json TEXT NOT NULL,
                lifetime_days INTEGER NOT NULL,
                type TEXT NOT NULL,
                budget_tier TEXT NOT NULL,
                note TEXT NOT NULL,
                updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """
        )
        connection.execute(
            """
            CREATE TABLE IF NOT EXISTS bouquets (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                price INTEGER NOT NULL,
                currency TEXT NOT NULL,
                image_resource TEXT NOT NULL,
                colors_json TEXT NOT NULL,
                flowers_json TEXT NOT NULL,
                composition_json TEXT NOT NULL DEFAULT '{}',
                wrapping_price INTEGER NOT NULL DEFAULT 55,
                best_for_json TEXT NOT NULL,
                events_json TEXT NOT NULL,
                style_json TEXT NOT NULL,
                note TEXT NOT NULL,
                updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """
        )
        connection.execute(
            """
            CREATE TABLE IF NOT EXISTS cart_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id TEXT NOT NULL,
                bouquet_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL DEFAULT 1,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(session_id, bouquet_id)
            )
            """
        )
        bouquet_columns = {
            row["name"]
            for row in connection.execute("PRAGMA table_info(bouquets)").fetchall()
        }
        session_columns = {
            row["name"]
            for row in connection.execute("PRAGMA table_info(sessions)").fetchall()
        }
        if "user_id" not in session_columns:
            connection.execute("ALTER TABLE sessions ADD COLUMN user_id INTEGER")
        if "title" not in session_columns:
            connection.execute("ALTER TABLE sessions ADD COLUMN title TEXT NOT NULL DEFAULT 'Новый чат'")
        if "composition_json" not in bouquet_columns:
            connection.execute(
                "ALTER TABLE bouquets ADD COLUMN composition_json TEXT NOT NULL DEFAULT '{}'"
            )
        if "wrapping_price" not in bouquet_columns:
            connection.execute(
                "ALTER TABLE bouquets ADD COLUMN wrapping_price INTEGER NOT NULL DEFAULT 55"
            )
        for flower in flowers_catalog:
            connection.execute(
                """
                INSERT INTO flowers (
                    slug, name, price, currency, colors_json, meaning_json,
                    lifetime_days, type, budget_tier, note, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT(slug) DO UPDATE SET
                    name = excluded.name,
                    price = excluded.price,
                    currency = excluded.currency,
                    colors_json = excluded.colors_json,
                    meaning_json = excluded.meaning_json,
                    lifetime_days = excluded.lifetime_days,
                    type = excluded.type,
                    budget_tier = excluded.budget_tier,
                    note = excluded.note,
                    updated_at = CURRENT_TIMESTAMP
                """,
                (
                    flower["slug"],
                    flower["name"],
                    flower["price"],
                    flower["currency"],
                    json.dumps(flower.get("colors", []), ensure_ascii=False),
                    json.dumps(flower.get("meaning", []), ensure_ascii=False),
                    flower.get("lifetime_days", 0),
                    flower.get("type", ""),
                    flower.get("budget_tier", ""),
                    flower.get("recommendation_note", ""),
                ),
            )
        for bouquet in existing_bouquets:
            composition = bouquet["composition"]
            flower_slugs = list(composition.keys())
            bouquet_price = calculate_bouquet_price(composition)
            connection.execute(
                """
                INSERT INTO bouquets (
                    id, name, price, currency, image_resource, colors_json,
                    flowers_json, composition_json, wrapping_price, best_for_json,
                    events_json, style_json, note, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT(id) DO UPDATE SET
                    name = excluded.name,
                    price = excluded.price,
                    currency = excluded.currency,
                    image_resource = excluded.image_resource,
                    colors_json = excluded.colors_json,
                    flowers_json = excluded.flowers_json,
                    composition_json = excluded.composition_json,
                    wrapping_price = excluded.wrapping_price,
                    best_for_json = excluded.best_for_json,
                    events_json = excluded.events_json,
                    style_json = excluded.style_json,
                    note = excluded.note,
                    updated_at = CURRENT_TIMESTAMP
                """,
                (
                    bouquet["id"],
                    bouquet["name"],
                    bouquet_price,
                    bouquet["currency"],
                    bouquet["image_resource"],
                    json.dumps(bouquet["colors"], ensure_ascii=False),
                    json.dumps(flower_slugs, ensure_ascii=False),
                    json.dumps(composition, ensure_ascii=False),
                    WRAPPING_AND_RIBBON_PRICE,
                    json.dumps(bouquet["best_for"], ensure_ascii=False),
                    json.dumps(bouquet["events"], ensure_ascii=False),
                    json.dumps(bouquet["style"], ensure_ascii=False),
                    bouquet["note"],
                ),
            )

        users = connection.execute("SELECT id, session_id FROM users").fetchall()
        for user in users:
            connection.execute(
                """
                INSERT INTO sessions (session_id, user_id, title, state_json, created_at, updated_at)
                VALUES (?, ?, 'Основной чат', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON CONFLICT(session_id) DO UPDATE SET
                    user_id = COALESCE(sessions.user_id, excluded.user_id),
                    title = CASE
                        WHEN sessions.title = 'Новый чат' THEN excluded.title
                        ELSE sessions.title
                    END
                """,
                (user["session_id"], user["id"], json.dumps(default_state(), ensure_ascii=False)),
            )


def hash_password(password: str, salt: str) -> str:
    raw = f"{salt}:{password}".encode("utf-8")
    return hashlib.sha256(raw).hexdigest()


def normalize_username(username: str) -> str:
    return username.strip().lower()


def auth_response(row: sqlite3.Row) -> dict[str, Any]:
    return {
        "user_id": row["id"],
        "username": row["username"],
        "session_id": row["session_id"],
    }


def create_user(username: str, password: str) -> dict[str, Any]:
    username = normalize_username(username)
    validation_error = validate_credentials(username, password)
    if validation_error:
        raise HTTPException(status_code=400, detail=validation_error)

    salt = uuid.uuid4().hex
    password_hash = hash_password(password, salt)

    row_data = None
    with db_connect() as connection:
        try:
            cursor = connection.execute(
                """
                INSERT INTO users (username, password_hash, salt, session_id)
                VALUES (?, ?, ?, ?)
                """,
                (username, password_hash, salt, "pending"),
            )
        except sqlite3.IntegrityError as exc:
            raise HTTPException(status_code=409, detail="Такой логин уже занят.") from exc

        user_id = cursor.lastrowid
        session_id = f"user-{user_id}"
        connection.execute(
            "UPDATE users SET session_id = ? WHERE id = ?",
            (session_id, user_id),
        )
        state = default_state()
        connection.execute(
            """
            INSERT INTO sessions (session_id, user_id, title, state_json, created_at, updated_at)
            VALUES (?, ?, 'Основной чат', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT(session_id) DO UPDATE SET
                user_id = excluded.user_id,
                title = excluded.title,
                state_json = excluded.state_json,
                updated_at = CURRENT_TIMESTAMP
            """,
            (session_id, user_id, json.dumps(state, ensure_ascii=False)),
        )
        row = connection.execute(
            "SELECT id, username, session_id FROM users WHERE id = ?",
            (user_id,),
        ).fetchone()
        row_data = auth_response(row)
        sessions[session_id] = state

    return row_data


def login_user(username: str, password: str) -> dict[str, Any]:
    username = normalize_username(username)
    with db_connect() as connection:
        row = connection.execute(
            """
            SELECT id, username, password_hash, salt, session_id
            FROM users
            WHERE username = ?
            """,
            (username,),
        ).fetchone()

    if row is None or hash_password(password, row["salt"]) != row["password_hash"]:
        raise HTTPException(status_code=401, detail="Неверный логин или пароль.")

    get_session(row["session_id"])
    return auth_response(row)


def validate_credentials(username: str, password: str) -> str | None:
    if len(username) < 6:
        return "Ник должен быть минимум 6 символов."
    if not all(char.isalnum() or char == "_" for char in username):
        return "В нике используйте буквы, цифры или _."
    if len(password) < 6:
        return "Пароль должен быть минимум 6 символов."
    if not any(char.isalpha() for char in password):
        return "Добавьте в пароль хотя бы одну букву."
    if not any(char.isdigit() for char in password):
        return "Добавьте в пароль хотя бы одну цифру."
    if not any(not char.isalnum() for char in password):
        return "Добавьте в пароль символ, например !."
    return None


def save_session(session_id: str, state: dict[str, Any]) -> None:
    sessions[session_id] = state
    with db_connect() as connection:
        connection.execute(
            """
            INSERT INTO sessions (session_id, state_json, created_at, updated_at)
            VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT(session_id) DO UPDATE SET
                state_json = excluded.state_json,
                updated_at = CURRENT_TIMESTAMP
            """,
            (session_id, json.dumps(state, ensure_ascii=False)),
        )


def make_chat_title(text: str) -> str:
    normalized = " ".join(text.strip().split())
    if not normalized:
        return "Новый чат"
    return normalized[:34] + ("..." if len(normalized) > 34 else "")


def maybe_update_chat_title(session_id: str, text: str) -> None:
    with db_connect() as connection:
        row = connection.execute(
            "SELECT title FROM sessions WHERE session_id = ?",
            (session_id,),
        ).fetchone()
        if row and row["title"] in {"Новый чат", "Основной чат"}:
            connection.execute(
                "UPDATE sessions SET title = ?, updated_at = CURRENT_TIMESTAMP WHERE session_id = ?",
                (make_chat_title(text), session_id),
            )


def create_chat(user_id: int, title: str | None = None) -> dict[str, Any]:
    session_id = f"chat-{user_id}-{uuid.uuid4().hex[:12]}"
    clean_title = title.strip() if title and title.strip() else "Новый чат"
    state = default_state()
    with db_connect() as connection:
        user = connection.execute("SELECT id FROM users WHERE id = ?", (user_id,)).fetchone()
        if user is None:
            raise HTTPException(status_code=404, detail="Пользователь не найден.")
        connection.execute(
            """
            INSERT INTO sessions (session_id, user_id, title, state_json, created_at, updated_at)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
            (session_id, user_id, clean_title, json.dumps(state, ensure_ascii=False)),
        )
    sessions[session_id] = state
    return {
        "session_id": session_id,
        "title": clean_title,
        "created_at": None,
        "updated_at": None,
        "message_count": 0,
    }


def get_user_chats(user_id: int) -> list[dict[str, Any]]:
    with db_connect() as connection:
        rows = connection.execute(
            """
            SELECT s.session_id, s.title, s.created_at, s.updated_at, COUNT(m.id) AS message_count
            FROM sessions s
            LEFT JOIN messages m ON m.session_id = s.session_id
            WHERE s.user_id = ?
            GROUP BY s.session_id
            ORDER BY s.updated_at DESC, s.created_at DESC
            """,
            (user_id,),
        ).fetchall()
    return [dict(row) for row in rows]


def delete_chat(user_id: int, session_id: str) -> dict[str, str]:
    with db_connect() as connection:
        row = connection.execute(
            "SELECT session_id FROM sessions WHERE session_id = ? AND user_id = ?",
            (session_id, user_id),
        ).fetchone()
        if row is None:
            raise HTTPException(status_code=404, detail="Чат не найден.")
        connection.execute("DELETE FROM cart_items WHERE session_id = ?", (session_id,))
        connection.execute("DELETE FROM messages WHERE session_id = ?", (session_id,))
        connection.execute("DELETE FROM sessions WHERE session_id = ?", (session_id,))
    sessions.pop(session_id, None)
    return {"status": "deleted"}


def add_message(session_id: str, role: str, text: str, intent: str | None = None) -> None:
    with db_connect() as connection:
        connection.execute(
            """
            INSERT INTO messages (session_id, role, text, intent)
            VALUES (?, ?, ?, ?)
            """,
            (session_id, role, text, intent),
        )


def load_session_from_db(session_id: str) -> dict[str, Any] | None:
    with db_connect() as connection:
        row = connection.execute(
            "SELECT state_json FROM sessions WHERE session_id = ?",
            (session_id,),
        ).fetchone()
    if row is None:
        return None
    return json.loads(row["state_json"])


def get_messages(session_id: str) -> list[dict[str, Any]]:
    with db_connect() as connection:
        rows = connection.execute(
            """
            SELECT id, role, text, intent, created_at
            FROM messages
            WHERE session_id = ?
            ORDER BY id
            """,
            (session_id,),
        ).fetchall()
    return [dict(row) for row in rows]


def get_flowers_from_db() -> list[dict[str, Any]]:
    with db_connect() as connection:
        rows = connection.execute(
            """
            SELECT slug, name, price, currency, colors_json, meaning_json,
                   lifetime_days, type, budget_tier, note
            FROM flowers
            ORDER BY name
            """
        ).fetchall()
    return [
        {
            **dict(row),
            "colors": json.loads(row["colors_json"]),
            "meaning": json.loads(row["meaning_json"]),
        }
        for row in rows
    ]


def get_bouquets_from_db() -> list[dict[str, Any]]:
    with db_connect() as connection:
        rows = connection.execute(
            """
            SELECT id, name, price, currency, image_resource, colors_json,
                   flowers_json, composition_json, wrapping_price, best_for_json,
                   events_json, style_json, note
            FROM bouquets
            ORDER BY id
            """
        ).fetchall()
    return [
        {
            "id": row["id"],
            "name": row["name"],
            "price": row["price"],
            "currency": row["currency"],
            "image_resource": row["image_resource"],
            "colors": json.loads(row["colors_json"]),
            "flowers": json.loads(row["flowers_json"]),
            "composition": json.loads(row["composition_json"]),
            "wrapping_price": row["wrapping_price"],
            "best_for": json.loads(row["best_for_json"]),
            "events": json.loads(row["events_json"]),
            "style": json.loads(row["style_json"]),
            "note": row["note"],
        }
        for row in rows
    ]


def get_bouquet_by_id(bouquet_id: int) -> dict[str, Any] | None:
    for bouquet in get_bouquets_from_db():
        if bouquet["id"] == bouquet_id:
            return bouquet
    return None


def add_bouquet_to_cart(session_id: str, bouquet_id: int) -> None:
    with db_connect() as connection:
        connection.execute(
            """
            INSERT INTO cart_items (session_id, bouquet_id, quantity)
            VALUES (?, ?, 1)
            ON CONFLICT(session_id, bouquet_id) DO UPDATE SET
                quantity = quantity + 1
            """,
            (session_id, bouquet_id),
        )


def get_cart_items(session_id: str) -> list[dict[str, Any]]:
    with db_connect() as connection:
        rows = connection.execute(
            """
            SELECT c.bouquet_id, c.quantity, b.name, b.price, b.currency, b.image_resource
            FROM cart_items c
            JOIN bouquets b ON b.id = c.bouquet_id
            WHERE c.session_id = ?
            ORDER BY c.id
            """,
            (session_id,),
        ).fetchall()
    return [dict(row) for row in rows]


def normalize_text(text: str) -> str:
    text = text.lower().replace("ё", "е")
    text = re.sub(r"[^а-яa-z0-9\s?]", " ", text)
    return re.sub(r"\s+", " ", text).strip()


def get_session(session_id: str) -> dict[str, Any]:
    if session_id in sessions:
        sessions[session_id].setdefault("skipped_slots", [])
        sessions[session_id].setdefault("pending_bouquet_id", None)
        sessions[session_id].setdefault("pending_action", None)
        return sessions[session_id]
    state = load_session_from_db(session_id) or default_state()
    state.setdefault("skipped_slots", [])
    state.setdefault("pending_bouquet_id", None)
    state.setdefault("pending_action", None)
    sessions[session_id] = state
    return state


def is_unknown_answer(text: str, intent: str) -> bool:
    normalized = normalize_text(text)
    words = normalized.split()
    if intent == "unknown" and len(words) <= 5:
        return True
    if intent == "unknown" and any(marker == normalized for marker in UNKNOWN_MARKERS):
        return True
    return any(
        normalized == marker or normalized.startswith(f"{marker} ")
        for marker in UNKNOWN_MARKERS
        if len(words) <= 5
    )


def is_affirmative(text: str) -> bool:
    normalized = normalize_text(text)
    yes_markers = [
        "да",
        "ага",
        "угу",
        "конечно",
        "давай",
        "можно",
        "хочу",
        "интересно",
        "расскажи",
        "подойдет",
        "добавь",
        "добавить",
        "беру",
        "ок",
        "окей",
    ]
    no_markers = ["нет", "не надо", "не хочу", "потом", "не сейчас"]
    return any(marker in normalized for marker in yes_markers) and not any(marker in normalized for marker in no_markers)


def is_negative_answer(text: str) -> bool:
    normalized = normalize_text(text)
    return any(marker in normalized for marker in ["нет", "не надо", "не хочу", "потом", "не сейчас", "без ухода"])


def is_bouquet_info_request(text: str) -> bool:
    normalized = normalize_text(text)
    info_markers = [
        "расскажи",
        "подробнее",
        "инфо",
        "информация",
        "что за",
        "что входит",
        "состав",
        "опиши",
        "описание",
        "про первый",
        "про второй",
        "про третий",
        "о первом",
        "о втором",
        "о третьем",
    ]
    return any(marker in normalized for marker in info_markers) and any(
        marker in normalized
        for marker in ["букет", "вариант", "перв", "втор", "трет", "noir", "crimson", "blue", "ghost", "pink", "lavender", "mist", "orchid", "reverie"]
    )


def detect_feedback_intent(text: str, predicted_intent: str) -> str:
    normalized = normalize_text(text)
    if is_bouquet_info_request(text):
        return "bouquet_info_request"

    customize_markers = [
        "замени",
        "заменить",
        "убери",
        "убрать",
        "без ",
        "поменяй",
        "переделай",
        "измени",
        "изменить",
        "добавь",
        "добавить",
        "сделай нежнее",
        "сделай ярче",
        "сделай светлее",
        "сделай темнее",
        "сделай пышнее",
        "сделай дешевле",
        "сделай дороже",
        "кастом",
    ]
    cart_markers = ["в корзину", "покуп", "оформ", "беру"]
    customize_exclusions = ["без повода", "без разницы", "без предпочтений", "без понятия"]
    if (
        predicted_intent == "customize_request"
        or (
            any(marker in normalized for marker in customize_markers)
            and not any(marker in normalized for marker in cart_markers)
            and not any(marker in normalized for marker in customize_exclusions)
        )
    ):
        return "customize_request"

    care_markers = [
        "ухаж",
        "уход",
        "сохран",
        "простоит",
        "стоит",
        "дольше",
        "вянет",
        "оживить",
        "воду",
        "подрез",
        "срез",
        "вазу",
        "сушить",
        "подкорм",
        "пакетик",
    ]
    if predicted_intent == "care_advice" or any(marker in normalized for marker in care_markers):
        return "care_advice"

    positive_markers = [
        "мне нравится",
        "мне нравиться",
        "нравится",
        "нравиться",
        "понрав",
        "беру",
        "добавь",
        "добавить",
        "в корзину",
        "хочу этот",
        "давай этот",
        "подходит",
        "идеально",
        "класс",
        "супер",
        "окей",
        "ок добавь",
    ]
    negative_markers = [
        "не нравится",
        "не понрав",
        "не подходит",
        "не то",
        "не хочу",
        "другой",
        "другое",
        "дешевле",
        "дороже",
        "не мой",
        "не тот",
    ]
    select_markers = [
        "первый",
        "второй",
        "третий",
        "вариант один",
        "вариант два",
        "вариант три",
        "noir lavender",
        "crimson eclipse",
        "blue mist",
        "ghost orchid",
    ]
    if any(marker in normalized for marker in positive_markers):
        return "positive_feedback"
    if any(marker in normalized for marker in negative_markers):
        return "negative_feedback"
    if predicted_intent == "select_option" or any(marker in normalized for marker in select_markers):
        return "select_option"
    return predicted_intent


def select_bouquet_from_text(text: str, state: dict[str, Any]) -> dict[str, Any] | None:
    normalized = normalize_text(text)
    last_bouquets = state.get("last_bouquets", [])
    if not last_bouquets:
        return None

    option_index = None
    if any(marker in normalized for marker in ["первый", "вариант один", "номер один"]):
        option_index = 0
    elif any(marker in normalized for marker in ["второй", "вариант два", "номер два"]):
        option_index = 1
    elif any(marker in normalized for marker in ["третий", "вариант три", "номер три"]):
        option_index = 2

    if option_index is not None and option_index < len(last_bouquets):
        return get_bouquet_by_id(last_bouquets[option_index]["id"])

    for bouquet in last_bouquets:
        if normalize_text(bouquet["name"]) in normalized:
            return get_bouquet_by_id(bouquet["id"])

    return get_bouquet_by_id(last_bouquets[0]["id"])


def extract_customization_changes(text: str) -> dict[str, list[str]]:
    normalized = normalize_text(text)
    changes = {"remove": [], "add": [], "style": []}
    flower_names = {
        flower["slug"]: flower["name"]
        for flower in flowers_catalog
    }
    for slug, aliases in flower_care_aliases.items():
        if any(normalize_text(alias) in normalized for alias in aliases):
            if any(marker in normalized for marker in ["убери", "убрать", "без", "не хочу", "не нравится"]):
                changes["remove"].append(flower_names.get(slug, slug))
            elif any(marker in normalized for marker in ["добавь", "добавить"]):
                changes["add"].append(flower_names.get(slug, slug))
            elif any(marker in normalized for marker in ["замени", "заменить", "поменяй"]):
                changes["remove"].append(flower_names.get(slug, slug))

    style_markers = {
        "нежнее": "сделать композицию нежнее и мягче",
        "ярче": "добавить более яркий акцент",
        "светлее": "перевести букет в более светлую гамму",
        "темнее": "сделать гамму глубже и драматичнее",
        "пышнее": "добавить объема",
        "дешевле": "собрать более бюджетную версию",
        "дороже": "сделать вариант более премиальным",
        "минималист": "сделать композицию спокойнее и лаконичнее",
        "романтич": "усилить романтичное настроение",
        "упаков": "изменить упаковку",
        "лент": "заменить ленту",
    }
    for marker, description in style_markers.items():
        if marker in normalized:
            changes["style"].append(description)
    return changes


def build_customize_reply(text: str, state: dict[str, Any]) -> dict[str, Any]:
    selected = select_bouquet_from_text(text, state)
    changes = extract_customization_changes(text)
    base_name = selected["name"] if selected else "выбранный вариант"
    missing_in_selected: list[str] = []
    if selected and changes["remove"]:
        composition_names = {
            next((item["name"] for item in flowers_catalog if item["slug"] == slug), slug).lower()
            for slug in selected.get("composition", {})
        }
        missing_in_selected = [
            name for name in changes["remove"]
            if name.lower() not in composition_names
        ]

    parts = []
    actual_remove = [name for name in changes["remove"] if name not in missing_in_selected]
    if actual_remove:
        parts.append("убрать: " + ", ".join(dict.fromkeys(actual_remove)))
    if changes["add"]:
        parts.append("добавить: " + ", ".join(dict.fromkeys(changes["add"])))
    if changes["style"]:
        parts.append("; ".join(dict.fromkeys(changes["style"])))

    if missing_in_selected:
        missing_text = ", ".join(dict.fromkeys(missing_in_selected))
        reply = (
            f"В {base_name} уже нет: {missing_text}. "
            "Если хочется уйти от похожего настроения, можно уточнить правку: например сделать букет нежнее, светлее, ярче или заменить упаковку."
        )
    elif parts:
        change_text = "; ".join(parts)
        reply = (
            f"Да, можно адаптировать {base_name}: {change_text}. "
            "Я бы собрала кастомную версию в том же настроении, но с учетом правок. "
            "Такой вариант лучше уточнить у флориста перед оплатой, потому что цена может измениться из-за замены цветов."
        )
    else:
        reply = (
            f"Да, {base_name} можно кастомизировать: заменить цветы, поменять упаковку, "
            "сделать букет нежнее, ярче, дешевле или премиальнее. Напишите, что именно убрать или добавить."
        )

    return {
        "reply": reply,
        "intent": "customize_request",
        "state": state,
        "recommendations": [],
        "bouquets": [selected] if selected else state.get("last_bouquets", []),
        "cart_added": None,
    }


def bouquet_composition_names(bouquet: dict[str, Any]) -> list[str]:
    return [
        next((item["name"] for item in flowers_catalog if item["slug"] == slug), slug)
        for slug in bouquet.get("composition", {})
    ]


def build_bouquet_info_reply(bouquet: dict[str, Any]) -> str:
    composition = bouquet_composition_names(bouquet)
    style = ", ".join(bouquet.get("style", [])[:3])
    colors = ", ".join(bouquet.get("colors", [])[:3])
    best_for = ", ".join(recipient_display.get(item, item) for item in bouquet.get("best_for", [])[:4])
    return (
        f"{bouquet['name']} — {bouquet['price']} {bouquet['currency']}.\n"
        f"Состав: {', '.join(composition)}.\n"
        f"Настроение: {style}. Гамма: {colors}.\n"
        f"Хорошо подойдет для: {best_for}.\n"
        f"{bouquet.get('note', '')}\n\n"
        "Желаете узнать, как ухаживать за этим букетом?"
    ).strip()


def build_cart_question(bouquet: dict[str, Any]) -> str:
    return f"Добавить {bouquet['name']} в корзину?"


def handle_pending_action(
    text: str,
    state: dict[str, Any],
    session_id: str,
) -> dict[str, Any] | None:
    bouquet_id = state.get("pending_bouquet_id")
    pending_action = state.get("pending_action")
    if not bouquet_id or not pending_action:
        return None

    bouquet = get_bouquet_by_id(bouquet_id)
    if not bouquet:
        state["pending_bouquet_id"] = None
        state["pending_action"] = None
        return None

    if pending_action == "offer_info":
        if is_negative_answer(text):
            state["pending_action"] = "confirm_cart"
            reply = build_cart_question(bouquet)
        elif is_affirmative(text) or is_bouquet_info_request(text):
            state["pending_action"] = "offer_care"
            reply = build_bouquet_info_reply(bouquet)
        else:
            return None
        return {
            "reply": reply,
            "intent": pending_action,
            "state": state,
            "recommendations": [],
            "bouquets": [bouquet],
            "cart_added": None,
        }

    if pending_action == "offer_care":
        if is_affirmative(text):
            state["pending_action"] = "confirm_cart"
            care_reply = build_care_advice_reply(bouquet["name"], state)
            reply = f"{care_reply}\n\n{build_cart_question(bouquet)}"
        elif is_negative_answer(text):
            state["pending_action"] = "confirm_cart"
            reply = build_cart_question(bouquet)
        else:
            return None
        return {
            "reply": reply,
            "intent": pending_action,
            "state": state,
            "recommendations": [],
            "bouquets": [bouquet],
            "cart_added": None,
        }

    if pending_action == "confirm_cart":
        if is_affirmative(text):
            add_bouquet_to_cart(session_id, bouquet["id"])
            state["pending_bouquet_id"] = None
            state["pending_action"] = None
            reply = (
                f"Готово, добавила {bouquet['name']} в корзину. "
                "Добавленный товар можно посмотреть в корзине."
            )
            return {
                "reply": reply,
                "intent": pending_action,
                "state": state,
                "recommendations": [],
                "bouquets": [bouquet],
                "cart_added": bouquet,
            }
        if is_negative_answer(text):
            state["pending_bouquet_id"] = None
            state["pending_action"] = None
            reply = "Ок, не добавляю. Можем подобрать другой вариант или изменить этот букет."
            return {
                "reply": reply,
                "intent": pending_action,
                "state": state,
                "recommendations": [],
                "bouquets": state.get("last_bouquets", []),
                "cart_added": None,
            }
    return None


def handle_bouquet_info_request(
    text: str,
    state: dict[str, Any],
) -> dict[str, Any] | None:
    selected = select_bouquet_from_text(text, state)
    if not selected:
        return None
    state["pending_bouquet_id"] = selected["id"]
    state["pending_action"] = "offer_care"
    return {
        "reply": build_bouquet_info_reply(selected),
        "intent": "bouquet_info_request",
        "state": state,
        "recommendations": [],
        "bouquets": [selected],
        "cart_added": None,
    }


def handle_feedback(
    text: str,
    intent: str,
    state: dict[str, Any],
    session_id: str,
) -> dict[str, Any] | None:
    if intent not in {"positive_feedback", "negative_feedback", "select_option"}:
        return None
    if not state.get("last_bouquets"):
        return None

    selected = select_bouquet_from_text(text, state)

    if intent in {"positive_feedback", "select_option"} and selected:
        state["pending_bouquet_id"] = selected["id"]
        state["pending_action"] = "offer_info"
        reply = (
            f"Отлично, {selected['name']} выглядит подходящим вариантом. "
            "Рассказать подробнее про состав, настроение и кому этот букет лучше подойдет?"
        )
        return {
            "reply": reply,
            "intent": intent,
            "state": state,
            "recommendations": [],
            "bouquets": [selected],
            "cart_added": None,
        }

    if intent == "negative_feedback":
        if selected:
            rejected = state.setdefault("rejected_bouquet_ids", [])
            if selected["id"] not in rejected:
                rejected.append(selected["id"])
        alternatives = recommend_existing_bouquets(state, limit=3)
        state["last_bouquets"] = alternatives
        names = ", ".join(f"{item['name']} ({item['price']} {item['currency']})" for item in alternatives)
        reply = (
            f"Поняла, этот вариант убираю. Предложу другие: {names}."
            if alternatives
            else "Поняла, этот вариант убираю. Давайте уточним цвет или бюджет, и я подберу лучше."
        )
        return {
            "reply": reply,
            "intent": intent,
            "state": state,
            "recommendations": [],
            "bouquets": alternatives,
            "cart_added": None,
        }

    return None


def next_missing_slot(state: dict[str, Any]) -> str | None:
    skipped_slots = set(state.get("skipped_slots", []))
    for slot in QUESTION_SLOTS:
        if not state.get(slot) and slot not in skipped_slots:
            return slot
    return None


def skip_slot(state: dict[str, Any], slot: str | None) -> None:
    if slot is None:
        return
    skipped_slots = state.setdefault("skipped_slots", [])
    if slot not in skipped_slots:
        skipped_slots.append(slot)


def extract_budget(text: str) -> int | None:
    numbers = [int(match) for match in re.findall(r"\d+", text)]
    if not numbers:
        return None
    realistic = [number for number in numbers if number >= 50]
    return max(realistic or numbers)


def extract_age(text: str) -> int | None:
    numbers = [int(match) for match in re.findall(r"\d+", text)]
    candidates = [number for number in numbers if 1 <= number <= 100]
    return candidates[0] if candidates else None


def extract_age_or_type(text: str) -> int | str | None:
    age = extract_age(text)
    if age is not None:
        return age
    normalized = normalize_text(text)
    type_markers = [
        "девочка",
        "девушка",
        "женщина",
        "мужчина",
        "парень",
        "мальчик",
        "бабушка",
        "дедушка",
        "ребенок",
        "подросток",
        "пожилой мужчина",
        "пожилая женщина",
    ]
    for marker in type_markers:
        if marker in normalized:
            return marker
    return None


def extract_recipient(text: str) -> str | None:
    normalized = normalize_text(text)
    self_markers = ["для себя", "себе", "для меня", "хочу себе", "себя"]
    if any(
        re.search(rf"(?<![а-яa-z0-9]){re.escape(marker)}(?![а-яa-z0-9])", normalized)
        for marker in self_markers
    ):
        return "для себя"
    return extract_from_aliases(text, recipient_aliases)


def extract_from_aliases(text: str, aliases: dict[str, list[str]]) -> str | None:
    normalized = normalize_text(text)
    for canonical, variants in aliases.items():
        candidates = [canonical, *variants]
        for variant in candidates:
            variant_normalized = normalize_text(variant)
            if (
                variant_normalized
                and re.search(
                    rf"(?<![а-яa-z0-9]){re.escape(variant_normalized)}(?![а-яa-z0-9])",
                    normalized,
                )
            ):
                return canonical
    return None


def extract_color(text: str) -> str | None:
    normalized = normalize_text(text)
    for color, aliases in color_aliases.items():
        candidates = [color, *aliases]
        if any(normalize_text(candidate) in normalized for candidate in candidates):
            return color

    known_colors = {
        color
        for flower in flowers_catalog
        for color in flower.get("colors", [])
    }
    for color in known_colors:
        if normalize_text(color) in normalized:
            return color
    return None


def extract_event(text: str) -> str | None:
    normalized = normalize_text(text)
    events = {
        event
        for flower in flowers_catalog
        for event in flower.get("events", [])
    }
    event_aliases = {
        "день рождения": ["др", "днюха", "на день рождения"],
        "годовщина": ["годовщина отношений", "на годовщину"],
        "свидание": ["первое свидание", "для свидания", "романтический вечер"],
        "без повода": ["просто так", "для сюрприза", "в знак внимания"],
        "извинение": ["извиниться", "попросить прощения", "помириться"],
        "благодарность": ["спасибо", "поблагодарить", "сказать спасибо"],
        "8 марта": ["к 8 марта", "на 8 марта"],
        "свадьба": ["на свадьбу", "свадебный"],
        "юбилей": ["на юбилей"],
    }

    for event, aliases in event_aliases.items():
        if any(normalize_text(alias) in normalized for alias in [event, *aliases]):
            return event
    for event in events:
        if normalize_text(event) in normalized:
            return event
    return None


def find_flower_for_care(text: str) -> dict[str, Any] | None:
    normalized = normalize_text(text)
    for flower in flowers_catalog:
        slug = flower["slug"]
        aliases = [flower["name"], slug, *flower_care_aliases.get(slug, [])]
        if any(normalize_text(alias) in normalized for alias in aliases):
            return flower
    return None


def find_bouquet_for_care(text: str, state: dict[str, Any]) -> dict[str, Any] | None:
    normalized = normalize_text(text)
    for bouquet in get_bouquets_from_db():
        if normalize_text(bouquet["name"]) in normalized:
            return bouquet

    last_bouquets = state.get("last_bouquets", [])
    if last_bouquets and any(marker in normalized for marker in ["этот", "этого", "букет", "композиция"]):
        return get_bouquet_by_id(last_bouquets[0]["id"])
    return None


def build_care_advice_reply(text: str, state: dict[str, Any]) -> str:
    bouquet = find_bouquet_for_care(text, state)
    flower = find_flower_for_care(text)

    general_tips = (
        "Базовый уход: поставьте букет в чистую вазу, налейте прохладную воду, "
        "обновите срезы под углом на 1-2 см, уберите листья ниже уровня воды, "
        "меняйте воду каждый день и держите букет вдали от солнца, батареи, сквозняка и фруктов."
    )

    if bouquet:
        composition = bouquet.get("composition", {})
        flower_names = [
            next((item["name"] for item in flowers_catalog if item["slug"] == slug), slug)
            for slug in composition
        ]
        lifetimes = [
            item.get("lifetime_days", 0)
            for item in flowers_catalog
            if item["slug"] in composition
        ]
        lifetime_text = (
            f"Ориентир по стойкости: около {min(lifetimes)}-{max(lifetimes)} дней."
            if lifetimes
            else "Ориентир по стойкости зависит от свежести цветов и температуры."
        )
        focus_tip = ""
        if "hydrangea" in composition:
            focus_tip += " Гортензию особенно важно хорошо поить: она быстрее других реагирует на нехватку воды."
        if "lily" in composition:
            focus_tip += " У лилий лучше убрать пыльники."
        if any(slug in composition for slug in ["lavender", "eucalyptus", "statice", "protea"]):
            focus_tip += " Лаванду, эвкалипт, статицу и протею можно потом красиво высушить."
        return (
            f"Для {bouquet['name']} уход такой: {general_tips} "
            f"В составе: {', '.join(flower_names)}. {lifetime_text}{focus_tip}"
        )

    if flower:
        lifetime = flower.get("lifetime_days")
        lifetime_text = f" Обычно {flower['name'].lower()} держится около {lifetime} дней." if lifetime else ""
        specific_tip = specific_care_tips.get(flower["slug"], "")
        return f"{general_tips} {lifetime_text} {specific_tip}".strip()

    return (
        f"{general_tips} Если букет начал вянуть, снова подрежьте стебли, вымойте вазу "
        "и поставьте цветы в прохладное место на несколько часов. Самые стойкие варианты обычно: "
        "гвоздики, хризантемы, орхидеи, лаванда, эвкалипт, протея и статица."
    )


def update_state(state: dict[str, Any], text: str, intent: str) -> None:
    previous_missing_slot = next_missing_slot(state)
    recipient = extract_recipient(text)
    color = extract_color(text)
    event = extract_event(text)
    budget = extract_budget(text)
    age_or_type = extract_age_or_type(text)

    if recipient and (previous_missing_slot != "age" or not state.get("recipient")):
        state["recipient"] = recipient
    if color:
        state["color"] = color
    if event:
        state["event"] = event
    if budget and (intent == "answer_budget" or "лей" in normalize_text(text) or "бюджет" in normalize_text(text)):
        state["budget"] = budget

    if intent == "answer_age" or (previous_missing_slot == "age" and age_or_type is not None):
        state["age"] = age_or_type
    elif intent == "answer_eye_color":
        state["eye_color"] = text.strip()
    elif intent == "answer_years_together":
        state["years_together"] = text.strip()
    elif intent == "answer_budget" and budget:
        state["budget"] = budget
    elif intent == "answer_color" and color:
        state["color"] = color
    elif intent == "answer_event" and event:
        state["event"] = event
    elif intent == "answer_recipient" and recipient and (previous_missing_slot != "age" or not state.get("recipient")):
        state["recipient"] = recipient

    if is_unknown_answer(text, intent):
        slot_to_skip = previous_missing_slot
        if slot_to_skip and state.get(slot_to_skip):
            slot_to_skip = next_missing_slot(state)
        skip_slot(state, slot_to_skip)


def budget_tier_for_amount(amount: int | None) -> str | None:
    if amount is None:
        return None
    for tier, rule in budget_rules.items():
        max_value = rule["max"]
        if amount >= rule["min"] and (max_value is None or amount <= max_value):
            return tier
    return None


def score_flower(flower: dict[str, Any], state: dict[str, Any]) -> int:
    score = 0
    recipient = state.get("recipient")
    profile = recipient_recommendations.get(recipient or "", {})
    recommended_slugs = profile.get("flowers", [])
    recommended_colors = profile.get("colors", [])

    if flower["slug"] in recommended_slugs:
        score += 7
    if recipient and recipient in flower.get("best_for", []):
        score += 4

    color = state.get("color")
    if color and color in flower.get("colors", []):
        score += 5
    if color and color in recommended_colors:
        score += 2

    event = state.get("event")
    if event and event in flower.get("events", []):
        score += 4

    budget_tier = budget_tier_for_amount(state.get("budget"))
    if budget_tier and budget_tier == flower.get("budget_tier"):
        score += 3
    elif budget_tier == "эконом" and flower.get("price", 0) <= 50:
        score += 2

    if flower.get("lifetime_days", 0) >= 9:
        score += 1
    return score


def recommend_flowers(state: dict[str, Any], limit: int = 3) -> list[dict[str, Any]]:
    scored = sorted(
        flowers_catalog,
        key=lambda flower: (score_flower(flower, state), -flower.get("price", 0)),
        reverse=True,
    )
    recommendations = []
    for flower in scored[:limit]:
        recommendations.append(
            {
                "name": flower["name"],
                "slug": flower["slug"],
                "price": flower["price"],
                "currency": flower["currency"],
                "colors": flower["colors"],
                "meaning": flower["meaning"],
                "lifetime_days": flower["lifetime_days"],
                "note": flower["recommendation_note"],
            }
        )
    return recommendations


def score_existing_bouquet(bouquet: dict[str, Any], state: dict[str, Any]) -> int:
    score = 0
    recipient = state.get("recipient")
    event = state.get("event")
    color = state.get("color")
    budget = state.get("budget")

    if recipient and recipient in bouquet.get("best_for", []):
        score += 7
    if event and event in bouquet.get("events", []):
        score += 5
    if color and color in bouquet.get("colors", []):
        score += 5
    if budget:
        if bouquet["price"] <= budget:
            score += 4
        else:
            score -= 5
    return score


def recommend_existing_bouquets(state: dict[str, Any], limit: int = 2) -> list[dict[str, Any]]:
    bouquets = get_bouquets_from_db()
    rejected_ids = set(state.get("rejected_bouquet_ids", []))
    bouquets = [bouquet for bouquet in bouquets if bouquet["id"] not in rejected_ids]
    scored = sorted(
        bouquets,
        key=lambda bouquet: (score_existing_bouquet(bouquet, state), -bouquet["price"]),
        reverse=True,
    )
    return [bouquet for bouquet in scored[:limit] if score_existing_bouquet(bouquet, state) > 0]


def next_question(state: dict[str, Any]) -> str | None:
    skipped_slots = set(state.get("skipped_slots", []))
    if not state.get("recipient") and "recipient" not in skipped_slots:
        return "Для кого подбираем букет? Можно написать: для себя, мама, девушка, мужчина, бабушка, коллега или любимый человек."
    if not state.get("age") and "age" not in skipped_slots:
        return "Сколько лет получателю? Можно примерно: 25, девушка, мужчина, девочка, бабушка или дедушка."
    if not state.get("event") and "event" not in skipped_slots:
        return "Какой повод: день рождения, благодарность, свидание, годовщина или просто без повода?"
    if not state.get("color") and "color" not in skipped_slots:
        return "Есть любимый цвет или оттенок? Если не знаете, я подберу гамму сам."
    if not state.get("budget") and "budget" not in skipped_slots:
        return "На какой бюджет ориентироваться в леях? Можно примерно: до 500, до 1000, до 2000."
    return None


def build_recommendation_reply(state: dict[str, Any]) -> str:
    recommendations = recommend_flowers(state)
    bouquet_recommendations = recommend_existing_bouquets(state)
    names = ", ".join(item["name"] for item in recommendations)
    recipient = recipient_display.get(state.get("recipient"), state.get("recipient") or "вашего случая")
    event = state.get("event") or "без конкретного повода"
    budget = state.get("budget")
    age = state.get("age")
    profile = recipient_recommendations.get(state.get("recipient") or "", {})
    note = profile.get("note", "Подобрала варианты по вашим ответам: по получателю, поводу, цвету и бюджету.")
    context_parts = [f"для {recipient}", f"повод: {event}"]
    if age:
        context_parts.append(f"возраст/тип: {age}")
    if budget:
        context_parts.append(f"бюджет: до {budget} лей")

    reply = (
        "Нашла спокойный вариант под ваш запрос.\n"
        f"Ориентир: {', '.join(context_parts)}.\n\n"
        f"Если собирать букет вручную, я бы взяла: {names}. "
        f"{note}"
    )
    affordable_bouquets = [
        bouquet for bouquet in bouquet_recommendations
        if budget is None or bouquet["price"] <= budget
    ]
    if affordable_bouquets:
        ready_names = "\n".join(
            f"{index + 1}. {bouquet['name']} — {bouquet['price']} {bouquet['currency']}"
            for index, bouquet in enumerate(affordable_bouquets)
        )
        reply += (
            "\n\nИз готового ассортимента лучше всего подходят:\n"
            f"{ready_names}\n\n"
            "Можно написать: «подробнее про первый», «мне нравится второй» или «сделай нежнее»."
        )
    else:
        reply += (
            "\n\nГотового букета под все условия может не быть, "
            "но можно собрать кастомную композицию и отдельно подобрать упаковку."
        )
    return reply


def bot_reply(text: str, session_id: str = "default") -> dict[str, Any]:
    normalized = normalize_text(text)
    intent = detect_feedback_intent(text, model.predict([normalized])[0])
    state = get_session(session_id)
    add_message(session_id, "user", text, intent)
    maybe_update_chat_title(session_id, text)

    pending_response = handle_pending_action(text, state, session_id)
    if pending_response is not None:
        save_session(session_id, state)
        add_message(session_id, "assistant", pending_response["reply"])
        return pending_response

    if intent == "bouquet_info_request":
        info_response = handle_bouquet_info_request(text, state)
        if info_response is not None:
            save_session(session_id, state)
            add_message(session_id, "assistant", info_response["reply"])
            return info_response

    feedback_response = handle_feedback(text, intent, state, session_id)
    if feedback_response is not None:
        save_session(session_id, state)
        add_message(session_id, "assistant", feedback_response["reply"])
        return feedback_response

    if intent == "care_advice":
        reply = build_care_advice_reply(text, state)
        save_session(session_id, state)
        add_message(session_id, "assistant", reply)
        return {
            "reply": reply,
            "intent": intent,
            "state": state,
            "recommendations": [],
            "bouquets": [],
            "cart_added": None,
        }

    if intent == "customize_request":
        response = build_customize_reply(text, state)
        save_session(session_id, state)
        add_message(session_id, "assistant", response["reply"])
        return response

    update_state(state, text, intent)

    recommendations: list[dict[str, Any]] = []
    bouquet_recommendations: list[dict[str, Any]] = []
    question = next_question(state)

    if intent in {"greeting", "bouquet_request"} and question:
        reply = question
    elif question:
        reply = f"Понял. {question}"
    else:
        recommendations = recommend_flowers(state)
        bouquet_recommendations = recommend_existing_bouquets(state)
        state["last_bouquets"] = bouquet_recommendations
        reply = build_recommendation_reply(state)

    save_session(session_id, state)
    add_message(session_id, "assistant", reply)

    return {
        "reply": reply,
        "intent": intent,
        "state": state,
        "recommendations": recommendations,
        "bouquets": bouquet_recommendations,
        "cart_added": None,
    }


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/reset")
def reset(request: ChatRequest) -> dict[str, str]:
    sessions.pop(request.session_id, None)
    with db_connect() as connection:
        connection.execute("DELETE FROM sessions WHERE session_id = ?", (request.session_id,))
        connection.execute("DELETE FROM messages WHERE session_id = ?", (request.session_id,))
    return {"status": "reset"}


@app.post("/chat")
def chat(request: ChatRequest) -> dict[str, Any]:
    return bot_reply(request.text, request.session_id)


@app.post("/register")
def register(request: AuthRequest) -> dict[str, Any]:
    return create_user(request.username, request.password)


@app.post("/login")
def login(request: AuthRequest) -> dict[str, Any]:
    return login_user(request.username, request.password)


@app.get("/history/{session_id}")
def history(session_id: str) -> dict[str, Any]:
    return {
        "session_id": session_id,
        "state": get_session(session_id),
        "messages": get_messages(session_id),
    }


@app.get("/users/{user_id}/chats")
def user_chats(user_id: int) -> dict[str, Any]:
    items = get_user_chats(user_id)
    if not items:
        user = None
        with db_connect() as connection:
            user = connection.execute("SELECT id, session_id FROM users WHERE id = ?", (user_id,)).fetchone()
        if user is None:
            raise HTTPException(status_code=404, detail="Пользователь не найден.")
        create_chat(user_id, "Новый чат")
        items = get_user_chats(user_id)
    return {"count": len(items), "items": items}


@app.post("/users/{user_id}/chats")
def new_user_chat(user_id: int, request: CreateChatRequest) -> dict[str, Any]:
    return create_chat(user_id, request.title)


@app.delete("/users/{user_id}/chats/{session_id}")
def remove_user_chat(user_id: int, session_id: str) -> dict[str, str]:
    result = delete_chat(user_id, session_id)
    if not get_user_chats(user_id):
        create_chat(user_id, "Новый чат")
    return result


@app.get("/flowers")
def flowers() -> dict[str, Any]:
    items = get_flowers_from_db()
    return {"count": len(items), "items": items}


@app.get("/bouquets")
def bouquets() -> dict[str, Any]:
    items = get_bouquets_from_db()
    return {"count": len(items), "items": items}


@app.get("/cart/{session_id}")
def cart(session_id: str) -> dict[str, Any]:
    items = get_cart_items(session_id)
    return {"count": len(items), "items": items}


init_database()
