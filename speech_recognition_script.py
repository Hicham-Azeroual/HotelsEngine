import speech_recognition as sr
import json
import spacy

# Load the spaCy model
nlp = spacy.load("en_core_web_sm")

def recognize_speech():
    recognizer = sr.Recognizer()
    with sr.Microphone() as source:
        print("Please speak now. Listening for speech...")
        recognizer.adjust_for_ambient_noise(source, duration=1)  # Adjust for ambient noise
        try:
            # Listen for up to 20 seconds or until speech is detected
            audio = recognizer.listen(source, timeout=20, phrase_time_limit=20)
        except sr.WaitTimeoutError:
            return {"error": "No speech detected within the timeout period"}

    try:
        result = recognizer.recognize_google(audio, language="en-US")  # Specify language
        # Extract key-value pairs using spaCy
        extracted_info = extract_key_value_pairs(result)
        # Return a JSON object with the recognized text and extracted information
        return {"recognized_text": result, "extracted_info": extracted_info}
    except sr.UnknownValueError:
        return {"error": "Google Speech Recognition could not understand audio"}
    except sr.RequestError as e:
        return {"error": f"Could not request results from Google Speech Recognition service; {e}"}

def extract_key_value_pairs(text):
    doc = nlp(text)
    extracted_info = {}

    # Extract city and hotel names (you can customize this logic based on your needs)
    for ent in doc.ents:
        if ent.label_ == "GPE" or ent.label_ == "LOC":  # GPE for geopolitical entities (cities, countries)
            extracted_info["city"] = ent.text
        elif ent.label_ == "ORG":  # ORG for organizations (hotels can be considered as organizations)
            extracted_info["hotel"] = ent.text

    return extracted_info

if __name__ == "__main__":
    result = recognize_speech()
    if "error" in result:
        print(result["error"])
    else:
        print("Recognized Text:", result["recognized_text"])
        print("Extracted Information:", result["extracted_info"])
    with open("voice_recognition_result.json", "w") as file:
        json.dump(result, file)