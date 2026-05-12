from __future__ import annotations

import csv
from pathlib import Path


DATASET_PATH = Path(r"C:\all\mine\utm_vibe\python_utm\dataset_intents.csv")


KEEP_INTENT_BY_TEXT = {
    "6 месяцев отношений": "answer_event",
    "год отношений": "answer_event",
    "первая годовщина отношений": "answer_event",
    "первый месяц отношений": "answer_event",
    "полгода отношений": "answer_event",
    "третья годовщина": "answer_event",
    "не актуально": "unknown",
    "не подходит": "negative_feedback",
    "нужен бюджетный букет": "bouquet_request",
    "хочу blue mist": "positive_feedback",
    "хочу crimson eclipse": "positive_feedback",
    "хочу ghost orchid": "positive_feedback",
    "хочу noir lavender": "positive_feedback",
}


def main() -> None:
    with DATASET_PATH.open("r", encoding="utf-8-sig", newline="") as file:
        rows = [
            {"text": row["text"].strip().lower(), "intent": row["intent"].strip()}
            for row in csv.DictReader(file)
            if row.get("text") and row.get("intent")
        ]

    cleaned = []
    removed = []
    seen = set()
    for row in rows:
        expected_intent = KEEP_INTENT_BY_TEXT.get(row["text"])
        if expected_intent and row["intent"] != expected_intent:
            removed.append(row)
            continue
        key = (row["text"], row["intent"])
        if key in seen:
            removed.append(row)
            continue
        seen.add(key)
        cleaned.append(row)

    with DATASET_PATH.open("w", encoding="utf-8", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=["text", "intent"])
        writer.writeheader()
        writer.writerows(cleaned)

    counts = {}
    for row in cleaned:
        counts[row["intent"]] = counts.get(row["intent"], 0) + 1

    print(f"removed={len(removed)}")
    print(f"rows={len(cleaned)}")
    for intent, count in sorted(counts.items()):
        print(intent, count)


if __name__ == "__main__":
    main()
