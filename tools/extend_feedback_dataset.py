import csv
from pathlib import Path


DATASET_PATH = Path(r"C:\all\mine\utm_vibe\python_utm\dataset_intents.csv")

NEW_ROWS = {
    "positive_feedback": [
        "нравится",
        "мне нравится",
        "мне понравилось",
        "класс",
        "супер",
        "отлично",
        "беру",
        "давай этот",
        "давай его",
        "хочу этот",
        "хочу его",
        "подходит",
        "мне подходит",
        "то что надо",
        "идеально",
        "прекрасно",
        "красиво",
        "очень красиво",
        "хороший вариант",
        "оставим этот",
        "добавь в корзину",
        "добавить в корзину",
        "закажу этот",
        "выбираю этот",
        "мне нравится первый вариант",
        "мне нравится второй вариант",
        "первый нравится",
        "второй нравится",
        "этот букет нравится",
        "да это хороший букет",
        "окей добавь",
        "ок добавь",
        "согласен",
        "согласна",
        "подойдет",
        "можно этот",
        "берем первый",
        "берем второй",
        "хочу noir lavender",
        "хочу blue mist",
        "хочу ghost orchid",
        "хочу crimson eclipse",
    ],
    "negative_feedback": [
        "не нравится",
        "мне не нравится",
        "не понравилось",
        "не подходит",
        "не то",
        "не хочу этот",
        "не хочу такое",
        "покажи другой",
        "давай другой",
        "есть другой вариант",
        "что то другое",
        "предложи другое",
        "плохо",
        "не красиво",
        "слишком дорого",
        "дорого",
        "не по бюджету",
        "не мой стиль",
        "не тот цвет",
        "хочу нежнее",
        "хочу ярче",
        "хочу дешевле",
        "хочу дороже",
        "хочу другой цвет",
        "не подходит первый вариант",
        "не подходит второй вариант",
        "первый не нравится",
        "второй не нравится",
        "не добавляй",
        "не надо",
        "отмена",
        "передумал",
        "передумала",
        "давай без этого",
        "не нравится noir lavender",
        "не нравится blue mist",
        "не нравится ghost orchid",
        "не нравится crimson eclipse",
    ],
    "select_option": [
        "первый вариант",
        "второй вариант",
        "третий вариант",
        "первый",
        "второй",
        "третий",
        "вариант один",
        "вариант два",
        "вариант три",
        "номер один",
        "номер два",
        "номер три",
        "выбираю первый",
        "выбираю второй",
        "выбираю третий",
        "покажи первый",
        "покажи второй",
        "покажи третий",
        "noir lavender",
        "crimson eclipse",
        "blue mist",
        "ghost orchid",
    ],
}


def main() -> None:
    rows = []
    with DATASET_PATH.open("r", encoding="utf-8-sig", newline="") as file:
        reader = csv.DictReader(file)
        for row in reader:
            rows.append({"text": row["text"].strip().lower(), "intent": row["intent"].strip()})

    seen = {(row["text"], row["intent"]) for row in rows}
    for intent, texts in NEW_ROWS.items():
        for text in texts:
            item = (text.strip().lower(), intent)
            if item not in seen:
                rows.append({"text": item[0], "intent": item[1]})
                seen.add(item)

    with DATASET_PATH.open("w", encoding="utf-8", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=["text", "intent"])
        writer.writeheader()
        writer.writerows(rows)

    counts = {}
    for row in rows:
        counts[row["intent"]] = counts.get(row["intent"], 0) + 1

    print(f"rows={len(rows)}")
    for intent, count in sorted(counts.items()):
        print(intent, count)


if __name__ == "__main__":
    main()
