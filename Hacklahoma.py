from flask import Flask, jsonify
import os
import hashlib

app = Flask(__name__)

@app.route('/generate_hash', methods=['GET'])
def generate_hash():
    """Generates a random 64-byte hash."""
    random_bytes = os.urandom(64)  # Generate 64 random bytes
    hash_value = hashlib.sha256(random_bytes).hexdigest()  # Hash using SHA-256
    return jsonify({'hash': hash_value})

if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5000, debug=True)
