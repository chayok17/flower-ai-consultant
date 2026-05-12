from __future__ import annotations

import csv
from pathlib import Path


DATASET_PATH = Path(r"C:\all\mine\utm_vibe\python_utm\dataset_intents.csv")


RELABEL = {
    "что нибудь нежное": "bouquet_request",
    "что нибудь яркое": "bouquet_request",
    "хочу что нибудь нежное": "bouquet_request",
    "хочу что нибудь яркое": "bouquet_request",
    "десять лет отношений": "answer_years_together",
    "третья годовщина отношений": "answer_years_together",
    "не актуально": "unknown",
    "не важно уже": "unknown",
}


ADDITIONAL_ROWS = {
    "answer_eye_color": [
        "глаза голубые",
        "голубые глаза",
        "у нее голубые глаза",
        "у него голубые глаза",
        "глаза синие",
        "синие глаза",
        "темно синие глаза",
        "глаза зеленые",
        "зеленые глаза",
        "изумрудные глаза",
        "глаза карие",
        "карие глаза",
        "темно карие глаза",
        "светло карие глаза",
        "глаза серые",
        "серые глаза",
        "серо голубые глаза",
        "серо зеленые глаза",
        "глаза ореховые",
        "ореховые глаза",
        "цвет глаз голубой",
        "цвет глаз зеленый",
        "цвет глаз карий",
        "цвет глаз серый",
        "не знаю цвет глаз",
        "цвет глаз не знаю",
        "без привязки к глазам",
    ],
    "select_option": [
        "хочу noir lavender",
        "хочу crimson eclipse",
        "хочу blue mist",
        "хочу ghost orchid",
        "noir lavender хочу",
        "crimson eclipse хочу",
        "blue mist хочу",
        "ghost orchid хочу",
        "выбери noir lavender",
        "выбери crimson eclipse",
        "выбери blue mist",
        "выбери ghost orchid",
        "тот noir lavender",
        "тот crimson eclipse",
        "тот blue mist",
        "тот ghost orchid",
        "первый из готовых",
        "второй из готовых",
        "третий из готовых",
        "четвертый из готовых",
        "покажи первый",
        "покажи второй",
        "покажи третий",
        "покажи четвертый",
        "вариант номер один",
        "вариант номер два",
        "вариант номер три",
        "вариант номер четыре",
        "номер четыре",
        "четвертый вариант",
        "четвертый букет",
    ],
    "positive_feedback": [
        "то что надо",
        "это то что надо",
        "именно то что надо",
        "это то что нужно",
        "идеально",
        "прям идеально",
        "идеальный вариант",
        "идеальный букет",
        "мне понравилось",
        "очень понравилось",
        "да мне понравилось",
        "мне не очень нравится но подойдет",
        "давай добавим",
        "добавляем",
        "добавляй",
        "можно добавить",
        "добавь в корзину",
        "положи его в корзину",
        "хочу купить",
        "хочу оформить",
    ],
    "negative_feedback": [
        "давай без этого",
        "без этого",
        "лучше без этого",
        "мне не очень нравится",
        "не очень нравится",
        "не понравилось",
        "мне не понравилось",
        "не то что нужно",
        "это не то что нужно",
        "не то что надо",
        "это не то что надо",
        "плохой вариант",
        "неудачный вариант",
        "слишком дорого для меня",
        "очень дорого для меня",
        "цена не подходит",
        "дорого хочу дешевле",
    ],
    "unknown": [
        "на вкус флориста",
        "пусть решит флорист",
        "доверяю флористу",
        "как флорист посоветует",
        "как лучше",
        "как будет лучше",
        "эм",
        "ээ",
        "эээ",
        "мм",
        "ммм",
        "не хочу отвечать",
        "не буду отвечать",
        "пропускаем вопрос",
        "без ответа",
        "затрудняюсь",
        "не могу сказать",
    ],
    "answer_color": [
        "фуксия",
        "цвет фуксия",
        "яркая фуксия",
        "малиновый",
        "ягодный",
        "коралловый",
        "терракотовый",
        "оливковый",
        "мятный",
        "цвет мяты",
        "холодная гамма",
        "теплая гамма",
        "в теплых тонах",
        "в холодных тонах",
    ],
    "answer_event": [
        "для помолвки",
        "помолвка",
        "на помолвку",
        "букет для помолвки",
        "свадебное предложение",
        "предложение",
        "делаю предложение",
        "на предложение",
    ],
    "answer_years_together": [
        "десять лет отношений",
        "10 лет отношений",
        "третья годовщина отношений",
        "наша третья годовщина отношений",
        "третий год отношений",
        "мы три года в отношениях",
    ],
    "bouquet_request": [
        "я выбираю букет",
        "помоги выбрать букет",
        "выбираю букет",
        "надо выбрать букет",
        "что нибудь нежное",
        "что нибудь яркое",
        "что нибудь дорогое",
        "что нибудь свежее",
        "что нибудь воздушное",
        "что нибудь праздничное",
    ],
    "greeting": [
        "йо",
        "йоу",
        "хей",
        "хэллоу",
        "доброе утро",
        "добрый вечер",
    ],
}


def main() -> None:
    with DATASET_PATH.open("r", encoding="utf-8-sig", newline="") as file:
        rows = [
            {"text": row["text"].strip().lower(), "intent": row["intent"].strip()}
            for row in csv.DictReader(file)
            if row.get("text") and row.get("intent")
        ]

    relabeled = 0
    cleaned = []
    for row in rows:
        target = RELABEL.get(row["text"])
        if target and row["intent"] != target:
            row = {"text": row["text"], "intent": target}
            relabeled += 1
        cleaned.append(row)

    rows = []
    seen_text = {}
    for row in cleaned:
        if row["text"] in RELABEL and row["intent"] != RELABEL[row["text"]]:
            continue
        key = (row["text"], row["intent"])
        if key in seen_text:
            continue
        rows.append(row)
        seen_text[key] = True

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

    counts = {}
    for row in rows:
        counts[row["intent"]] = counts.get(row["intent"], 0) + 1

    print(f"relabeled={relabeled}")
    print(f"added={added}")
    print(f"rows={len(rows)}")
    for intent, count in sorted(counts.items()):
        print(intent, count)


if __name__ == "__main__":
    main()
