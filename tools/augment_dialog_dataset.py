from __future__ import annotations

import csv
from pathlib import Path


BASE_DIR = Path(r"C:\all\mine\utm_vibe\python_utm")
DATASET_PATH = BASE_DIR / "dataset_intents.csv"


NEW_ROWS = [
    ("для себя", "answer_recipient"),
    ("себе", "answer_recipient"),
    ("для меня", "answer_recipient"),
    ("хочу себе букет", "answer_recipient"),
    ("букет для себя", "answer_recipient"),
    ("подбираю себе", "answer_recipient"),
    ("мне самой", "answer_recipient"),
    ("мне самому", "answer_recipient"),
    ("девушка", "answer_age"),
    ("молодая девушка", "answer_age"),
    ("женщина", "answer_age"),
    ("взрослая женщина", "answer_age"),
    ("мужчина", "answer_age"),
    ("взрослый мужчина", "answer_age"),
    ("девочка", "answer_age"),
    ("мальчик", "answer_age"),
    ("ребенок", "answer_age"),
    ("подросток", "answer_age"),
    ("бабушка", "answer_age"),
    ("дедушка", "answer_age"),
    ("пожилая женщина", "answer_age"),
    ("пожилой мужчина", "answer_age"),
    ("ей 18", "answer_age"),
    ("ему 25", "answer_age"),
    ("мне 20", "answer_age"),
    ("около 30 лет", "answer_age"),
    ("примерно 45", "answer_age"),
    ("возраст 60", "answer_age"),
    ("расскажи подробнее про первый", "bouquet_info_request"),
    ("расскажи подробнее про второй", "bouquet_info_request"),
    ("расскажи подробнее про третий", "bouquet_info_request"),
    ("подробнее про первый вариант", "bouquet_info_request"),
    ("подробнее про второй вариант", "bouquet_info_request"),
    ("что входит в первый букет", "bouquet_info_request"),
    ("что входит во второй букет", "bouquet_info_request"),
    ("какой состав у третьего", "bouquet_info_request"),
    ("информация про blue mist", "bouquet_info_request"),
    ("расскажи про ghost orchid", "bouquet_info_request"),
    ("что за bouquet pink reverie", "bouquet_info_request"),
    ("опиши noir lavender", "bouquet_info_request"),
    ("что за второй букет", "bouquet_info_request"),
    ("хочу узнать состав", "bouquet_info_request"),
    ("можешь описать этот букет", "bouquet_info_request"),
    ("расскажи кому он подойдет", "bouquet_info_request"),
    ("интересна информация про букет", "bouquet_info_request"),
    ("расскажи про уход", "care_advice"),
    ("как ухаживать за этим букетом", "care_advice"),
    ("хочу узнать уход", "care_advice"),
    ("как сохранить этот букет дольше", "care_advice"),
    ("как ухаживать за blue mist", "care_advice"),
    ("да расскажи про уход", "care_advice"),
    ("нет уход не нужен", "negative_feedback"),
    ("не надо про уход", "negative_feedback"),
    ("да добавь в корзину", "positive_feedback"),
    ("добавь его в корзину", "positive_feedback"),
    ("хочу добавить выбранный букет", "positive_feedback"),
    ("беру этот букет", "positive_feedback"),
    ("пока не добавляй", "negative_feedback"),
    ("не добавляй в корзину", "negative_feedback"),
]


def main() -> None:
    with DATASET_PATH.open("r", encoding="utf-8-sig", newline="") as file:
        reader = csv.DictReader(file)
        rows = list(reader)
        fieldnames = reader.fieldnames or ["text", "intent"]

    existing = {(row["text"].strip().lower(), row["intent"].strip()) for row in rows}
    added = 0
    for text, intent in NEW_ROWS:
        key = (text.strip().lower(), intent)
        if key not in existing:
            rows.append({"text": text, "intent": intent})
            existing.add(key)
            added += 1

    with DATASET_PATH.open("w", encoding="utf-8", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)

    print(f"rows={len(rows)} added={added} path={DATASET_PATH}")


if __name__ == "__main__":
    main()
