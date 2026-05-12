import re

import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics import accuracy_score, f1_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.svm import LinearSVC


def normalize(text):
    text = str(text).lower().replace("ё", "е")
    text = re.sub(r"[^а-яa-z0-9\s?]", " ", text)
    return re.sub(r"\s+", " ", text).strip()


data = pd.read_csv(r"C:\all\mine\utm_vibe\python_utm\dataset_intents.csv").dropna()
data["text"] = data["text"].map(normalize)
data = data.drop_duplicates(["text", "intent"])
x_train, x_test, y_train, y_test = train_test_split(
    data.text,
    data.intent,
    test_size=0.2,
    random_state=42,
    stratify=data.intent,
)

best = None
for analyzer in ["char", "char_wb"]:
    for ngram_range in [(1, 4), (1, 5), (2, 4), (2, 5), (3, 5), (2, 6)]:
        for c in [0.4, 0.7, 1.0, 1.2, 1.6, 2.0, 3.0, 5.0]:
            model = Pipeline(
                [
                    (
                        "tfidf",
                        TfidfVectorizer(
                            analyzer=analyzer,
                            ngram_range=ngram_range,
                            sublinear_tf=True,
                        ),
                    ),
                    ("clf", LinearSVC(C=c, class_weight="balanced", random_state=42)),
                ]
            )
            model.fit(x_train, y_train)
            predictions = model.predict(x_test)
            acc = accuracy_score(y_test, predictions)
            f1 = f1_score(y_test, predictions, average="macro")
            row = (acc, f1, analyzer, ngram_range, c)
            if best is None or row > best:
                best = row
            print(f"acc={acc:.4f} f1={f1:.4f} analyzer={analyzer} ngram={ngram_range} C={c}")

print("BEST", best)
