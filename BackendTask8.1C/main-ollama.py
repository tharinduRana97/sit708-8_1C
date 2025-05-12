from flask import Flask, request, Response
import requests
import argparse

app = Flask(__name__)

# Ollama API endpoint and model configuration
OLLAMA_API_URL = "http://localhost:11434/api/generate"
MODEL = "llama3:latest"

# 🧠 Store conversation history globally
conversation_history = []

def check_ollama_server():
    try:
        response = requests.get("http://localhost:11434")
        return response.status_code == 200
    except requests.ConnectionError:
        print("Error: Could not connect to Ollama server. Ensure it is running with 'ollama serve'.")
        return False

@app.route('/')
def index():
    return "Welcome to the Ollama Chatbot API!"

@app.route('/chat', methods=['POST'])
def chat():
    global conversation_history

    user_message = request.form.get('userMessage') or request.get_data(as_text=True).strip()

    if not user_message:
        return Response("Error: userMessage cannot be empty", status=400, mimetype='text/plain')

    print("\nReceived Request:")
    print(f"userMessage: {user_message}")

    # 🧠 Add user message to history
    conversation_history.append(f"User: {user_message}")

    # 🧠 Create prompt with full chat history
    system_instruction = (
        "You are a helpful assistant. Always keep your answers short and to the point unless "
        "the user explicitly asks for more detail or explanation.\n"
    )
    full_prompt = system_instruction + "\n".join(conversation_history) + "\nAI:"

    payload = {
        "model": MODEL,
        "prompt": full_prompt,
        "stream": False,
        "options": {
            "temperature": 0.6,
            "top_p": 0.85,
            "num_predict": 200
        }
    }

    try:
        response = requests.post(OLLAMA_API_URL, json=payload)
        print(f"Ollama Response Status: {response.status_code}")
        print(f"Ollama Response Text: {response.text}")
        response.raise_for_status()
        result = response.json()
        raw_output = result.get("response", "").strip()
    except requests.RequestException as e:
        print(f"Error during Ollama API call: {str(e)}")
        raw_output = ""

    print(f"Raw Model Output: {raw_output}")

    conversation_history.append(f"AI: {raw_output}")

    if not raw_output or raw_output.isspace():
        raw_output = f"Sorry, I couldn't provide a relevant answer to: '{user_message}'. Please rephrase."

    print(f"Generated Response: {raw_output}\n")

    return Response(raw_output, mimetype='text/plain')

# Optional: Reset conversation history
@app.route('/reset', methods=['POST'])
def reset():
    global conversation_history
    conversation_history = []
    return Response("Conversation reset.", mimetype='text/plain')

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--port', type=int, default=5050, help='Specify the port number')
    args = parser.parse_args()

    if check_ollama_server():
        print(f"App running on port {args.port}")
        app.run(host='0.0.0.0', port=args.port)
    else:
        print("Exiting due to Ollama server unavailability.")
