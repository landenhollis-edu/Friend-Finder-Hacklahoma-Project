import requests

# Define the server URL
server_url = "http://127.0.0.1:5000/generate_hash"

# Send a GET request to the server
response = requests.get(server_url)

# Print the response
if response.status_code == 200:
    print("Generated Hash:", response.json()['hash'])
else:
    print("Error:", response.status_code, response.text)
