from __future__ import annotations

import csv
from pathlib import Path


DATASET_PATH = Path(r"C:\all\mine\utm_vibe\python_utm\dataset_intents.csv")


ROWS = {
    "answer_budget": [
        "не слишком дорого",
        "чтобы было не слишком дорого",
        "не очень дорого пожалуйста",
        "без большой суммы",
        "в пределах бюджета",
        "ограниченный бюджет",
        "максимум 700 лей",
        "максимум 900 лей",
        "максимум 1300 лей",
        "до 700",
        "до 900",
        "до 1300",
    ],
    "answer_eye_color": [
        "у нее серо зеленые глаза",
        "у нее серо голубые глаза",
        "у него темно карие глаза",
        "цвет глаз синий",
        "цвет глаз голубой",
        "цвет глаз зеленый",
        "цвет глаз карий",
        "глаза светлые",
        "глаза темные",
        "не помню цвет глаз",
        "не знаю какие глаза",
        "без учета цвета глаз",
    ],
    "answer_color": [
        "айвори",
        "цвет айвори",
        "оттенок айвори",
        "слоновая кость",
        "цвет слоновой кости",
        "молочный оттенок",
        "теплый белый",
        "холодный белый",
    ],
    "answer_event": [
        "просто так",
        "просто так без повода",
        "это просто так",
        "без особого повода",
        "6 месяцев отношений повод",
        "третья годовщина повод",
        "на третью годовщину",
    ],
    "answer_years_together": [
        "полгода",
        "пол года",
        "полгода вместе",
        "полгода отношений",
        "6 месяцев вместе",
        "6 месяцев отношений",
        "третья годовщина отношений",
        "третья годовщина вместе",
    ],
    "care_advice": [
        "посоветуй стойкие цветы",
        "какие цветы самые стойкие",
        "какие цветы долго стоят",
        "что дольше всего стоит",
        "цветы которые долго живут",
        "самые стойкие букеты",
    ],
    "customize_request": [
        "не хочу каллы",
        "каллы не хочу",
        "убери каллы",
        "без калл пожалуйста",
        "не хочу орхидеи",
        "не хочу розы",
        "не хочу лаванду",
        "не хочу эти цветы в составе",
    ],
    "greeting": [
        "есть кто",
        "есть кто нибудь",
        "вы на месте",
        "у меня вопрос",
        "можно задать вопрос",
        "ку",
        "ку ку",
        "эй есть кто",
    ],
    "negative_feedback": [
        "есть еще что нибудь",
        "есть еще варианты",
        "покажи что нибудь другое",
        "этот вариант не актуален",
        "уже не актуально",
        "не актуально уже",
    ],
    "positive_feedback": [
        "подойдет",
        "да подойдет",
        "мне подойдет",
        "оставляю этот",
        "оставляю выбор на этом",
        "это то что нужно",
        "да это то что нужно",
        "хочу crimson eclipse",
        "хочу noir lavender",
        "хочу blue mist",
        "хочу ghost orchid",
        "да первый вариант",
        "да второй вариант",
        "да третий вариант",
    ],
    "unknown": [
        "любой вариант",
        "оставляю выбор тебе",
        "выбор за тобой",
        "на твое усмотрение",
        "не могу выбрать",
        "даже не знаю что выбрать",
    ],
}


KEEP_INTENT_BY_TEXT = {
    "просто так": "answer_event",
    "полгода": "answer_years_together",
    "полгода отношений": "answer_years_together",
    "6 месяцев отношений": "answer_years_together",
    "третья годовщина": "answer_event",
    "не хочу каллы": "customize_request",
    "есть кто": "greeting",
    "у меня вопрос": "greeting",
    "ку": "greeting",
    "ку ку": "greeting",
    "есть еще что нибудь": "negative_feedback",
    "уже не актуально": "negative_feedback",
    "подойдет": "positive_feedback",
    "это то что нужно": "positive_feedback",
    "любой вариант": "unknown",
    "оставляю выбор тебе": "unknown",
}


def main() -> None:
    with DATASET_PATH.open("r", encoding="utf-8-sig", newline="") as file:
        rows = [
            {"text": row["text"].strip().lower(), "intent": row["intent"].strip()}
            for row in csv.DictReader(file)
            if row.get("text") and row.get("intent")
        ]

    cleaned = []
    for row in rows:
        expected = KEEP_INTENT_BY_TEXT.get(row["text"])
        if expected and row["intent"] != expected:
            continue
        cleaned.append(row)

    rows = []
    seen = set()
    for row in cleaned:
        key = (row["text"], row["intent"])
        if key not in seen:
            rows.append(row)
            seen.add(key)

    added = 0
    for intent, texts in ROWS.items():
        for text in texts:
            item = (text.strip().lower(), intent)
            if item not in seen:
                rows.append({"text": item[0], "intent": item[1]})
                seen.add(item)
                added += 1

    with DATASET_PATH.open("w", encoding="utf-8", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=["text", "intent"])
        writer.writeheader()
        writer.writerows(rows)

    by_text = {}
    for row in rows:
        by_text.setdefault(row["text"], set()).add(row["intent"])
    conflicts = {text: intents for text, intents in by_text.items() if len(intents) > 1}
    print(f"added={added}")
    print(f"rows={len(rows)}")
    print(f"conflicts={len(conflicts)}")


if __name__ == "__main__":
    main()
