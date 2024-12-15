package org.example.hotelssearch.utils;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeocodingService {

    public static GPSCoordinates getLocationCoordinates(String query) throws Exception {
        String apiKey = "767db7f3973a6052ca356b2266e5a73ebeb881f0a3486f07062d1a540d7975f2";
        String apiUrl = "https://serpapi.com/search?engine=google_maps&q=" + query + "&type=search&api_key=" + apiKey;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the JSON response
        JSONObject jsonResponse = new JSONObject(response.body());

        // Extract the gps_coordinates from the place_results
        JSONObject placeResults = jsonResponse.getJSONObject("place_results");
        JSONObject gpsCoordinates = placeResults.getJSONObject("gps_coordinates");

        // Create and return a GPSCoordinates object
        return new GPSCoordinates(gpsCoordinates.getDouble("latitude"), gpsCoordinates.getDouble("longitude"));
    }

    public static void main(String[] args) {
        try {
            String city = "fes";
            GPSCoordinates result = getLocationCoordinates(city);
            System.out.println("GPS Coordinates: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}