from flask import Flask, request, jsonify
import joblib

app = Flask(__name__)

model = joblib.load("random_forest_v2.joblib")

@app.route('/predict', methods=['POST'])
def predict():
    try:
        data = request.get_json()
        prediction = model.predict(data['features'])
        return jsonify({'prediction': prediction.tolist()})
    except Exception as e:
        return jsonify({'error': str(e)})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080)
