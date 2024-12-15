package org.example.hotelssearch.utils;

import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class GeocodingService {

    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyBKlP0OtG_BidvHX49sV1NSBwV659xqauo"; // Replace with your actual Google Maps API key

    public static GPSCoordinates getLocationCoordinates(String query) throws Exception {
        // Trim the query string to remove any leading or trailing whitespace
        query = query.trim();

        // Encode the query string to handle special characters
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String apiUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=" + encodedQuery + "&key=" + GOOGLE_MAPS_API_KEY;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
        JSONObject jsonResponse = new JSONObject(response.body());

        // Check if the response contains results
        if (jsonResponse.getString("status").equals("OK")) {
            // Extract the first result
            JSONObject location = jsonResponse.getJSONArray("results")
                    .getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONObject("location");

            // Create and return a GPSCoordinates object
            return new GPSCoordinates(location.getDouble("lat"), location.getDouble("lng"));
        } else {
            throw new Exception("Failed to retrieve coordinates: " + jsonResponse.getString("status") + " for query: " + query);
        }
    }

    public static void main(String[] args) {
        try {
            String city = "New York";
            GPSCoordinates result = getLocationCoordinates(city);
            System.out.println("GPS Coordinates: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}