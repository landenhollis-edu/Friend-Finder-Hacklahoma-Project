from flask import Flask, jsonify
import os
import hashlib
import requests
import json

# Initialize Flask app
app = Flask(__name__)

# Ethereum JSON-RPC node (Ganache, Infura, or Alchemy)
ETH_NODE_URL = "http://127.0.0.1:7545"  # Change for Infura/Alchemy

# Ethereum Account (Replace with your actual account)
SENDER_ADDRESS = "0x459712F0e40B15E0A6C25F823A6509e7f6537794"
PRIVATE_KEY = "0x57c1e660905c774a3b5749e782aaec50a27453144cd3fe9a5719349a74dcb410y" 

# Smart Contract Details
CONTRACT_ADDRESS = "0xad7D3781C8c7CEC9430D6Bda7DA5B986bb0fcd08"
CONTRACT_ABI = [...]  # Replace with your contract's ABI


def send_rpc_request(method, params=[]):
    """Send a JSON-RPC request to Ethereum node."""
    headers = {"Content-Type": "application/json"}
    payload = {
        "jsonrpc": "2.0",
        "method": method,
        "params": params,
        "id": 1,
    }
    response = requests.post(ETH_NODE_URL, json=payload, headers=headers)
    return response.json()


@app.route("/generate_wallet", methods=["GET"])
def generate_wallet():
    """Generate a new Ethereum wallet address."""
    response = send_rpc_request("personal_newAccount", ["password123"])  # Example password
    return jsonify({"new_wallet": response.get("result", "Error generating wallet")})


@app.route("/generate_hash", methods=["GET"])
def generate_hash():
    """Generate a random 64-byte hash."""
    random_bytes = os.urandom(64)
    hash_value = hashlib.sha256(random_bytes).hexdigest()
    return jsonify({"hash": hash_value})


@app.route("/generate_and_store_hash", methods=["GET"])
def generate_and_store_hash():
    """Generate a hash and send a transaction to store it on Ethereum."""
    random_bytes = os.urandom(64)
    hash_value = hashlib.sha256(random_bytes).hexdigest()
    hash_bytes = "0x" + hash_value[:64]  # Convert to Ethereum-compatible bytes32

    # Build and send transaction (low-level JSON-RPC call)
    txn_params = {
        "from": SENDER_ADDRESS,
        "to": CONTRACT_ADDRESS,
        "gas": "2000000",
        "gasPrice": "0x5d21dba00",  # Example gas price (adjust as needed)
        "data": "0x" + hash_bytes,  # Data payload (hash)
    }
    txn_response = send_rpc_request("eth_sendTransaction", [txn_params])
    
    return jsonify({"hash": hash_value, "transaction": txn_response.get("result", "Error")})


@app.route("/get_hash/<address>", methods=["GET"])
def get_hash(address):
    """Retrieve a stored hash from the Ethereum blockchain."""
    # JSON-RPC request to call the contract function
    call_params = {
        "to": CONTRACT_ADDRESS,
        "data": "0x"  # Function selector for `getHash(address)` should be added here
    }
    response = send_rpc_request("eth_call", [call_params, "latest"])
    
    return jsonify({"stored_hash": response.get("result", "Error")})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
