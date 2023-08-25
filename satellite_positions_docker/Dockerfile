FROM python:3.9



RUN pip install --no-use-pep517 joblib flask
RUN pip install requests
RUN pip install skyfield
RUN pip install numpy

COPY sat_positions.py /app/sat_position.py


WORKDIR /app


EXPOSE 8081


CMD ["python", "sat_position.py"]