import re

import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics import accuracy_score
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

model = Pipeline(
    [
        ("tfidf", TfidfVectorizer(analyzer="char_wb", ngram_range=(2, 5), sublinear_tf=True)),
        ("clf", LinearSVC(C=1.2, class_weight="balanced", random_state=42)),
    ]
)
model.fit(x_train, y_train)
predictions = model.predict(x_test)
print("accuracy", accuracy_score(y_test, predictions))
for text, true, pred in zip(x_test, y_test, predictions):
    if true != pred:
        print(f"{text}\tTRUE={true}\tPRED={pred}")
