from __future__ import annotations

import csv
from pathlib import Path


DATASET_PATH = Path(r"C:\all\mine\utm_vibe\python_utm\dataset_intents.csv")


ROWS = {
    "answer_age": [
        "жене 30 лет",
        "жене тридцать",
        "ей 30 лет",
        "маме 50 лет",
        "папе 55 лет",
        "пожилой мужчина возраст",
        "получатель пожилой мужчина",
        "получательница пожилая женщина",
        "возраст пожилой",
        "человек пожилой",
    ],
    "answer_years_together": [
        "6 лет",
        "7 лет",
        "8 лет",
        "9 лет",
        "шесть лет отношений",
        "семь лет отношений",
        "восемь лет отношений",
        "девять лет отношений",
        "мы 7 лет вместе",
        "уже 7 лет вместе",
        "первый месяц отношений",
        "первый месяц вместе",
    ],
    "answer_recipient": [
        "букет врачу",
        "нужен букет врачу",
        "хочу букет врачу",
        "букет для врача",
        "подарок врачу",
        "для пожилого мужчины",
        "для пожилой женщины",
    ],
    "answer_color": [
        "на вкус флориста по цвету",
        "цвет на вкус флориста",
        "любой цвет на вкус флориста",
        "не тот цвет",
        "цвет не тот",
        "хочу другой цвет",
    ],
    "bouquet_request": [
        "какие цветы взять",
        "какие цветы лучше взять",
        "какие цветы выбрать",
        "что из цветов взять",
        "помоги выбрать цветы",
        "что взять из цветов",
    ],
    "care_advice": [
        "уход после покупки букета",
        "как ухаживать после покупки",
        "что делать после покупки букета",
        "что делать после доставки букета",
        "первый уход за букетом",
        "как подготовить букет после покупки",
    ],
    "customize_request": [
        "адаптируй этот букет",
        "адаптируй букет",
        "адаптируй вариант",
        "не нравится состав",
        "состав не нравится",
        "хочу другой состав",
        "поменяй состав",
        "сделай дешевле",
        "сделай подешевле",
        "сделай более бюджетно",
        "подгони под бюджет",
    ],
    "positive_feedback": [
        "да первый вариант",
        "да второй вариант",
        "да третий вариант",
        "да первый вариант беру",
        "да второй вариант беру",
        "да третий вариант беру",
        "это то что нужно",
        "именно это то что нужно",
        "хочу blue mist",
        "хочу noir lavender",
        "хочу ghost orchid",
        "хочу crimson eclipse",
    ],
    "select_option": [
        "eclipse",
        "crimson eclipse",
        "вариант eclipse",
        "покажи eclipse",
        "выбираю eclipse",
    ],
    "unknown": [
        "нет идей",
        "вообще нет идей",
        "любой подойдет",
        "любой вариант подойдет",
        "что?",
        "что",
        "не понял",
        "не поняла",
        "можно еще раз",
    ],
}


KEEP_INTENT_BY_TEXT = {
    "сделай дешевле": "customize_request",
    "не тот цвет": "answer_color",
    "это то что нужно": "positive_feedback",
    "не нравится состав": "customize_request",
    "хочу другой цвет": "answer_color",
    "первый месяц отношений": "answer_years_together",
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

    conflicts = {}
    by_text = {}
    for row in rows:
        by_text.setdefault(row["text"], set()).add(row["intent"])
    for text, intents in by_text.items():
        if len(intents) > 1:
            conflicts[text] = sorted(intents)

    print(f"added={added}")
    print(f"rows={len(rows)}")
    print(f"conflicts={len(conflicts)}")
    for text, intents in list(conflicts.items())[:20]:
        print(text, intents)


if __name__ == "__main__":
    main()
