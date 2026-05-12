from __future__ import annotations

import sys
from pathlib import Path

import joblib


BASE_DIR = Path(__file__).resolve().parent
MODEL_PATH = BASE_DIR / "intent_model.pkl"


def main() -> None:
    model = joblib.load(MODEL_PATH)
    text = " ".join(sys.argv[1:]).strip()
    if not text:
        text = input("Введите фразу: ").strip()
    intent = model.predict([text])[0]
    print(intent)


if __name__ == "__main__":
    main()
