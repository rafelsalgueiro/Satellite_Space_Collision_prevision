from flask import Flask, request, jsonify
from skyfield.api import Loader, EarthSatellite, wgs84
from datetime import timedelta

app = Flask(__name__)

@app.route('/calculate_coords', methods=['POST'])
def calculate_coords():
    data = request.json
    line1 = data['line1']
    line2 = data['line2']
    load = Loader('~/Documents/fishing/SkyData')
    ts = load.timescale()
    satellite = EarthSatellite(line1, line2)
    start_time = ts.now()
    end_time = start_time.utc_datetime() + timedelta(hours=1.2)
    interval = timedelta(minutes=0.5)
    coords = []
    current_time = start_time
    while current_time.utc_datetime() < end_time:
        position = satellite.at(current_time)
        lat, lon = wgs84.latlon_of(position)
        height = wgs84.height_of(position)

        if height.km < 0:
            height_km = height.km * -1
        else:
            height_km = height.km
        current_time = current_time + interval
        coords.append(f"{lon.degrees},{lat.degrees},{float(height_km) * 1000}")
    return jsonify({'positions': coords})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8081)