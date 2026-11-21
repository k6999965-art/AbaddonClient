
from flask import Flask, request, jsonify, render_template_string
from datetime import datetime
from threading import Lock

app = Flask(__name__)
lock = Lock()

devices = {}

HTML = """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Abaddon Control</title>
<style>
body {
    background:#101010;
    color:#eee;
    font-family:Arial;
    padding:30px;
}
h1 {color:#ff2525}
.card {
    background:#1b1b1b;
    padding:20px;
    margin:15px 0;
    border-left:4px solid #ff2525;
    border-radius:8px;
}
small {color:#888}
</style>
</head>
<body>
<h1>ABADDON CONTROL</h1>
<p>Android Remote Administration Dashboard</p>
<div id="devices">Loading...</div>
<script>
async function refresh() {
    const response = await fetch('/api/devices');
    const data = await response.json();

    document.getElementById('devices').innerHTML =
        Object.entries(data).map(([id,d]) => `
        <div class="card">
          <h2>${id}</h2>
          <p>Model: ${d.model}</p>
          <p>Android: ${d.android}</p>
          <p>Battery: ${d.battery}%</p>
          <p>Network: ${d.network}</p>
          <small>Last seen: ${d.last_seen}</small>
        </div>
        `).join('') || 'No connected devices';
}
refresh();
setInterval(refresh, 3000);
</script>
</body>
</html>
"""

@app.route("/")
def dashboard():
    return render_template_string(HTML)

@app.route("/api/register", methods=["POST"])
def register():
    data = request.get_json()

    required = ["device_id", "model", "android",
                "battery", "network"]

    if not isinstance(data, dict) or not all(
        field in data for field in required
    ):
        return jsonify({"error": "Invalid data"}), 400

    with lock:
        devices[data["device_id"]] = {
            "model": str(data["model"])[:100],
            "android": str(data["android"])[:30],
            "battery": int(data["battery"]),
            "network": str(data["network"])[:30],
            "last_seen": datetime.now().isoformat()
        }

    return jsonify({"status": "registered"})

@app.route("/api/devices")
def get_devices():
    with lock:
        return jsonify(devices)

if __name__ == "__main__":
    app.run(
        host="127.0.0.1",
        port=8080,
        debug=False
    )
