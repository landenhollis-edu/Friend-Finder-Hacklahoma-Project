from flask import Flask, jsonify
import os
import hashlib
from web3 import Web3

# Initialize Flask app
app = Flask(__name__)

# Connect to a local Ethereum node (Ganache)
ganache_url = "http://127.0.0.1:7545"  # Change this if using Infura/Alchemy
w3 = Web3(Web3.HTTPProvider(ganache_url))

# Ensure connection to Ethereum node
if not w3.is_connected():
    raise Exception("Cannot connect to Ethereum node!")

@app.route('/generate_wallet', methods=['GET'])
def generate_wallet():
    """Generate a new Ethereum wallet (address + private key)."""
    account = w3.eth.account.create()  # Creates a new Ethereum account
    return jsonify({
        'address': account.address,
        'private_key': account.key.hex()
    })

@app.route('/generate_hash', methods=['GET'])
def generate_hash():
    """Generate a random 64-byte hash."""
    random_bytes = os.urandom(64)  # Generate 64 random bytes
    hash_value = hashlib.sha256(random_bytes).hexdigest()
    return jsonify({'hash': hash_value})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract HashStore {
    mapping(address => bytes32) public storedHashes;

    event HashStored(address indexed user, bytes32 hashValue);

    function storeHash(bytes32 hashValue) public {
        storedHashes[msg.sender] = hashValue;
        emit HashStored(msg.sender, hashValue);
    }

    function getHash(address user) public view returns (bytes32) {
        return storedHashes[user];
    }
}
