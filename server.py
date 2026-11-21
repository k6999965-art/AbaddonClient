
from flask import Flask, request, jsonify

app = Flask(__name__)

@app.get("/health")
def health():
    return jsonify({
        "status": "online",
        "service": "Abaddon API"
    })

@app.post("/api/register")
def register():
    data = request.get_json(silent=True)

    if not isinstance(data, dict):
        return jsonify({
            "success": False,
            "error": "Invalid JSON"
        }), 400

    allowed_fields = {
        "device_id",
        "model",
        "android_version",
        "battery"
    }

    received = {
        key: data[key]
        for key in allowed_fields
        if key in data
    }

    print("[+] Consent-based test registration received")
    print(received)

    return jsonify({
        "success": True,
        "message": "Registration received"
    }), 200


if __name__ == "__main__":
    app.run(
        host="127.0.0.1",
        port=5000,
        debug=False
    )
