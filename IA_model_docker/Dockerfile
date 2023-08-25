FROM python:3.9

COPY random_forest_v2.joblib /app/random_forest_v2.joblib

RUN pip install --no-use-pep517 joblib flask
RUN pip install -U scikit-learn==1.2.2

COPY predictor.py /app/predictor.py


WORKDIR /app


EXPOSE 8080


CMD ["python", "predictor.py"]
