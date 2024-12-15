package org.example.hotelssearch.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetCurrentPosition {
    private static final String API_KEY = "AIzaSyBKlP0OtG_BidvHX49sV1NSBwV659xqauo"; // Replace with your API key

    public static GPSCoordinates getLocation() throws Exception {
        // URL to Google Maps Geolocation API
        String geolocationUrl = "https://www.googleapis.com/geolocation/v1/geolocate?key=" + API_KEY;

        // Create a connection to the Geolocation API
        HttpURLConnection geoConnection = (HttpURLConnection) new URL(geolocationUrl).openConnection();
        geoConnection.setRequestMethod("POST");
        geoConnection.setDoOutput(true); // POST request

        // Send the request body
        String body = "{\"considerIp\": \"true\"}";
        geoConnection.getOutputStream().write(body.getBytes("UTF-8"));

        // Read the response
        InputStreamReader geoReader = new InputStreamReader(geoConnection.getInputStream());
        JsonObject geoResponse = JsonParser.parseReader(geoReader).getAsJsonObject();

        // Extract latitude and longitude from the response
        JsonObject location = geoResponse.getAsJsonObject("location");
        double latitude = location.get("lat").getAsDouble();
        double longitude = location.get("lng").getAsDouble();

        // Return the GPS coordinates
        return new GPSCoordinates(latitude, longitude);
    }

    public static void main(String[] args) {
        try {
            GPSCoordinates coordinates = getLocation();
            System.out.println("Latitude: " + coordinates.getLatitude());
            System.out.println("Longitude: " + coordinates.getLongitude());
        } catch (Exception e) {
            System.err.println("An error occurred while fetching the location: " + e.getMessage());
            e.printStackTrace();
        }
    }
}