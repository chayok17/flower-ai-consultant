from __future__ import annotations

import csv
from pathlib import Path


DATASET_PATH = Path(r"C:\all\mine\utm_vibe\python_utm\dataset_intents.csv")


ADDITIONAL_ROWS = {
    "positive_feedback": [
        "точно хочу noir lavender",
        "точно хочу crimson eclipse",
        "точно хочу blue mist",
        "точно хочу ghost orchid",
        "хочу купить noir lavender",
        "хочу купить crimson eclipse",
        "хочу купить blue mist",
        "хочу купить ghost orchid",
        "добавь noir lavender",
        "добавь crimson eclipse",
        "добавь blue mist",
        "добавь ghost orchid",
        "добавь noir lavender в корзину",
        "добавь crimson eclipse в корзину",
        "добавь blue mist в корзину",
        "добавь ghost orchid в корзину",
        "беру ghost orchid",
        "беру crimson eclipse",
        "оформляю noir lavender",
        "оформляю blue mist",
        "давай купим этот",
        "давай купим первый",
        "давай купим второй",
        "давай купим третий",
        "давай купим четвертый",
    ],
    "unknown": [
        "без предпочтений",
        "нет предпочтений",
        "предпочтений нет",
        "не помню",
        "уже не помню",
        "не могу вспомнить",
        "вопрос сложный",
        "сложный вопрос",
        "можешь повторить",
        "повтори вопрос",
        "повтори пожалуйста",
        "что ты спросил",
        "что ты спросила",
        "я не понял вопрос",
        "я не поняла вопрос",
    ],
    "answer_color": [
        "ivory",
        "цвет ivory",
        "оттенок ivory",
        "слоновая кость",
        "цвет слоновой кости",
        "на твой вкус по цвету",
        "на вкус флориста по цвету",
    ],
    "answer_years_together": [
        "6 лет",
        "шесть лет",
        "полгода",
        "пол года",
        "мы полгода",
        "мы 6 лет",
    ],
    "greeting": [
        "ку привет",
        "ку ку привет",
        "эй привет",
        "йо привет",
    ],
    "negative_feedback": [
        "покажи другие букеты",
        "покажи другие варианты букетов",
        "покажи не эти букеты",
        "не эти букеты",
    ],
}


def main() -> None:
    with DATASET_PATH.open("r", encoding="utf-8-sig", newline="") as file:
        rows = [
            {"text": row["text"].strip().lower(), "intent": row["intent"].strip()}
            for row in csv.DictReader(file)
            if row.get("text") and row.get("intent")
        ]

    seen = {(row["text"], row["intent"]) for row in rows}
    added = 0
    for intent, texts in ADDITIONAL_ROWS.items():
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

    print(f"added={added}")
    print(f"rows={len(rows)}")


if __name__ == "__main__":
    main()
