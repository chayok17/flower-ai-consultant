from __future__ import annotations

import json
import re
from pathlib import Path

import joblib
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression, SGDClassifier
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix, f1_score
from sklearn.model_selection import StratifiedKFold, cross_val_score, train_test_split
from sklearn.naive_bayes import ComplementNB
from sklearn.pipeline import Pipeline
from sklearn.svm import LinearSVC


BASE_DIR = Path(__file__).resolve().parent
DATASET_PATH = BASE_DIR / "dataset_intents.csv"
MODEL_PATH = BASE_DIR / "intent_model.pkl"
REPORT_JSON_PATH = BASE_DIR / "intent_model_report.json"
REPORT_TXT_PATH = BASE_DIR / "intent_model_report.txt"


def normalize_text(text: str) -> str:
    text = str(text).lower().replace("ё", "е")
    text = re.sub(r"[^а-яa-z0-9\s?]", " ", text)
    text = re.sub(r"\s+", " ", text).strip()
    return text


def load_dataset(path: Path) -> pd.DataFrame:
    data = pd.read_csv(path)
    required_columns = {"text", "intent"}
    missing_columns = required_columns - set(data.columns)
    if missing_columns:
        raise ValueError(f"Нет нужных колонок: {sorted(missing_columns)}")

    data = data[["text", "intent"]].dropna()
    data["text"] = data["text"].map(normalize_text)
    data["intent"] = data["intent"].astype(str).str.strip()
    data = data[(data["text"] != "") & (data["intent"] != "")]
    data = data.drop_duplicates(subset=["text", "intent"]).reset_index(drop=True)
    return data


def build_candidates() -> dict[str, Pipeline]:
    def tfidf(analyzer: str, ngram_range: tuple[int, int]) -> TfidfVectorizer:
        return TfidfVectorizer(
            analyzer=analyzer,
            ngram_range=ngram_range,
            min_df=1,
            sublinear_tf=True,
        )

    return {
        "linear_svc": Pipeline(
            [
                ("tfidf", tfidf("char_wb", (2, 5))),
                ("clf", LinearSVC(C=0.7, class_weight="balanced", random_state=42)),
            ]
        ),
        "linear_svc_alt": Pipeline(
            [
                ("tfidf", tfidf("char_wb", (2, 4))),
                ("clf", LinearSVC(C=3.0, class_weight="balanced", random_state=42)),
            ]
        ),
        "logistic_regression": Pipeline(
            [
                ("tfidf", tfidf("char_wb", (2, 5))),
                (
                    "clf",
                    LogisticRegression(
                        C=4.0,
                        max_iter=3000,
                        class_weight="balanced",
                        random_state=42,
                    ),
                ),
            ]
        ),
        "sgd_log_loss": Pipeline(
            [
                ("tfidf", tfidf("char_wb", (2, 5))),
                (
                    "clf",
                    SGDClassifier(
                        loss="log_loss",
                        alpha=0.00005,
                        max_iter=2000,
                        class_weight="balanced",
                        random_state=42,
                    ),
                ),
            ]
        ),
        "complement_nb": Pipeline(
            [
                ("tfidf", tfidf("char_wb", (2, 5))),
                ("clf", ComplementNB(alpha=0.2)),
            ]
        ),
    }


def evaluate_model(name: str, model: Pipeline, x_train, x_test, y_train, y_test) -> dict:
    model.fit(x_train, y_train)
    predictions = model.predict(x_test)
    return {
        "name": name,
        "model": model,
        "accuracy": accuracy_score(y_test, predictions),
        "macro_f1": f1_score(y_test, predictions, average="macro"),
        "weighted_f1": f1_score(y_test, predictions, average="weighted"),
        "classification_report": classification_report(y_test, predictions, output_dict=True, zero_division=0),
        "confusion_matrix": confusion_matrix(y_test, predictions).tolist(),
    }


def main() -> None:
    data = load_dataset(DATASET_PATH)
    x = data["text"]
    y = data["intent"]

    x_train, x_test, y_train, y_test = train_test_split(
        x,
        y,
        test_size=0.2,
        random_state=42,
        stratify=y,
    )

    results = [
        evaluate_model(name, model, x_train, x_test, y_train, y_test)
        for name, model in build_candidates().items()
    ]
    best = max(results, key=lambda item: (item["accuracy"], item["macro_f1"]))

    cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=42)
    cv_accuracy = cross_val_score(best["model"], x, y, cv=cv, scoring="accuracy")
    cv_macro_f1 = cross_val_score(best["model"], x, y, cv=cv, scoring="f1_macro")

    final_model = build_candidates()[best["name"]]
    final_model.fit(x, y)
    joblib.dump(final_model, MODEL_PATH)

    report = {
        "dataset_path": str(DATASET_PATH),
        "rows": int(len(data)),
        "classes": sorted(y.unique().tolist()),
        "class_counts": data["intent"].value_counts().sort_index().to_dict(),
        "best_model": best["name"],
        "holdout_accuracy": round(best["accuracy"], 4),
        "holdout_macro_f1": round(best["macro_f1"], 4),
        "holdout_weighted_f1": round(best["weighted_f1"], 4),
        "cv_accuracy_mean": round(float(cv_accuracy.mean()), 4),
        "cv_accuracy_scores": [round(float(score), 4) for score in cv_accuracy],
        "cv_macro_f1_mean": round(float(cv_macro_f1.mean()), 4),
        "cv_macro_f1_scores": [round(float(score), 4) for score in cv_macro_f1],
        "candidate_scores": [
            {
                "name": item["name"],
                "accuracy": round(item["accuracy"], 4),
                "macro_f1": round(item["macro_f1"], 4),
                "weighted_f1": round(item["weighted_f1"], 4),
            }
            for item in sorted(results, key=lambda item: item["accuracy"], reverse=True)
        ],
        "classification_report": best["classification_report"],
        "confusion_matrix": best["confusion_matrix"],
    }

    REPORT_JSON_PATH.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")

    lines = [
        "Intent model training report",
        f"Dataset: {DATASET_PATH}",
        f"Rows: {report['rows']}",
        f"Classes: {len(report['classes'])}",
        f"Best model: {report['best_model']}",
        f"Holdout accuracy: {report['holdout_accuracy']:.2%}",
        f"Holdout macro F1: {report['holdout_macro_f1']:.2%}",
        f"5-fold CV accuracy: {report['cv_accuracy_mean']:.2%}",
        f"5-fold CV macro F1: {report['cv_macro_f1_mean']:.2%}",
        "",
        "Candidate scores:",
    ]
    for item in report["candidate_scores"]:
        lines.append(f"- {item['name']}: accuracy={item['accuracy']:.2%}, macro_f1={item['macro_f1']:.2%}")
    REPORT_TXT_PATH.write_text("\n".join(lines) + "\n", encoding="utf-8")

    print(f"Датасет: {DATASET_PATH}")
    print(f"Строк: {report['rows']}")
    print(f"Классов: {len(report['classes'])}")
    print(f"Лучшая модель: {report['best_model']}")
    print(f"Holdout accuracy: {report['holdout_accuracy']:.2%}")
    print(f"Holdout macro F1: {report['holdout_macro_f1']:.2%}")
    print(f"5-fold CV accuracy: {report['cv_accuracy_mean']:.2%}")
    print(f"Модель сохранена: {MODEL_PATH}")
    print(f"Отчет сохранен: {REPORT_TXT_PATH}")


if __name__ == "__main__":
    main()
