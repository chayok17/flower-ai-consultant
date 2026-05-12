from __future__ import annotations

import json
from collections import Counter, defaultdict

from sklearn.model_selection import train_test_split

import sys
from pathlib import Path


BACKEND_DIR = Path(r"C:\all\mine\utm_vibe\python_utm")
sys.path.insert(0, str(BACKEND_DIR))

from train_intent_model import build_candidates, load_dataset  # noqa: E402


def main() -> None:
    data = load_dataset(BACKEND_DIR / "dataset_intents.csv")
    x_train, x_test, y_train, y_test = train_test_split(
        data["text"],
        data["intent"],
        test_size=0.2,
        random_state=42,
        stratify=data["intent"],
    )

    report_path = BACKEND_DIR / "intent_model_report.json"
    model_name = "linear_svc_alt"
    if report_path.exists():
        with report_path.open("r", encoding="utf-8") as file:
            model_name = json.load(file).get("best_model", model_name)
    model = build_candidates()[model_name]
    model.fit(x_train, y_train)
    predictions = model.predict(x_test)

    errors = []
    pairs = Counter()
    by_true = defaultdict(list)
    for text, true, predicted in zip(x_test, y_test, predictions):
        if true == predicted:
            continue
        errors.append((text, true, predicted))
        pairs[(true, predicted)] += 1
        by_true[true].append((text, predicted))

    print(f"model={model_name}")
    print(f"errors={len(errors)} total={len(x_test)} accuracy={(len(x_test)-len(errors))/len(x_test):.2%}")
    print("\nTop confusions:")
    for (true, predicted), count in pairs.most_common():
        print(f"{true} -> {predicted}: {count}")

    print("\nErrors by true class:")
    for intent in sorted(by_true):
        print(f"\n[{intent}]")
        for text, predicted in by_true[intent]:
            print(f"- {text!r} -> {predicted}")


if __name__ == "__main__":
    main()
