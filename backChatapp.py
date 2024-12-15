from flask import Flask, request, jsonify
from flask_cors import CORS
import openai

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

# Set your OpenAI API key
# openai.api_key = "sk-aKWwxm_oncne4K4ggv-HlFJF0JIX0rY83g-Dlk1O0FT3BlbkFJ6SevrYLHPKNLQv96FOZG-u_XWH-Lsw_P91GfI65PcA"

@app.route('/chat', methods=['POST'])
def chat():
    try:
        # Get the user message from the request
        user_message = request.json.get('message', '')

        # Validate the request
        if not user_message:
            return jsonify({'error': 'Message is required'}), 400

        # Use OpenAI's GPT API to generate a response
        response = openai.ChatCompletion.create(
            model="gpt-3.5-turbo",  # Use GPT-3.5 or GPT-4
            messages=[
                {"role": "system", "content": "You are a helpful hotel assistant."},
                {"role": "user", "content": user_message}
            ]
        )

        # Extract the bot's response
        bot_response = response['choices'][0]['message']['content'].strip()

        # Print the bot's response in the backend console
        print(f"Bot Response: {bot_response}")

        return jsonify({'response': bot_response})

    except Exception as e:
        # Log the error and return a 500 error response
        print(f"Error: {e}")
        return jsonify({'error': 'Internal Server Error'}), 500

if __name__ == '__main__':
    # Run the Flask app on port 5000
    app.run(debug=True, port=5000)