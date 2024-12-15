import os
import json
import requests
from urllib.parse import urlparse
import hashlib
import time
import re  # Add this import

# Function to sanitize filenames to remove invalid characters
def sanitize_filename(filename):
    # Remove any character that is not a letter, digit, underscore, or dot
    sanitized = re.sub(r'[^\w\d_.]', '_', filename)
    return sanitized

# Function to generate a unique filename
def generate_unique_filename(url, save_dir):
    # Generate a hash of the URL
    url_hash = hashlib.md5(url.encode()).hexdigest()
    # Use the current timestamp to ensure uniqueness
    timestamp = str(int(time.time()))
    # Combine the hash and timestamp to form a unique filename
    unique_filename = f"{url_hash}_{timestamp}.jpg"
    return os.path.join(save_dir, unique_filename)

# Function to download an image from a URL and save it to a local directory
def download_image(url, save_dir):
    if not url:
        return None

    response = requests.get(url)
    if response.status_code == 200:
        # Extract the filename from the URL
        filename = os.path.basename(urlparse(url).path)
        # Sanitize the filename to remove invalid characters
        filename = sanitize_filename(filename)
        save_path = os.path.join(save_dir, filename)

        # If the filename is too long or invalid, generate a unique filename
        if len(filename) > 255 or not filename:
            save_path = generate_unique_filename(url, save_dir)

        with open(save_path, 'wb') as file:
            file.write(response.content)

        return save_path
    else:
        print(f"Failed to download image from {url}")
        return None

# Function to process the JSON data and download images
def process_json_data(json_data, save_dir):
    for hotel in json_data:
        images = hotel.get('images', [])
        local_image_paths = []

        for image_url in images:
            local_path = download_image(image_url, save_dir)
            if local_path:
                local_image_paths.append(local_path)

        # Update the JSON data with local image paths
        hotel['images'] = local_image_paths

    return json_data

# Main function to read JSON, process, and save updated JSON
def main():
    # Define the path to the JSON file and the directory to save images
    json_file_path = "C:/Users/EliteBook 840 G7/Desktop/filtered_output.json"
    save_dir = 'src/main/resources/images'

    # Ensure the save directory exists
    os.makedirs(save_dir, exist_ok=True)

    # Read the JSON data
    with open(json_file_path, 'r') as file:
        json_data = json.load(file)

    # Process the JSON data and download images
    updated_json_data = process_json_data(json_data, save_dir)

    # Save the updated JSON data back to the file
    with open(json_file_path, 'w') as file:
        json.dump(updated_json_data, file, indent=4)

if __name__ == "__main__":
    main()