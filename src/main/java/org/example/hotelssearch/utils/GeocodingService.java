package org.example.hotelssearch.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;

public class GeocodingService {

    private static final String API_KEY = "AIzaSyBKlP0OtG_BidvHX49sV1NSBwV659xqauo";
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


    public static String getDetailedLocation(GPSCoordinates gpsCoordinates) throws Exception {
        String apiUrl = String.format(Locale.US,
                "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s",
                gpsCoordinates.getLatitude(), gpsCoordinates.getLongitude(), API_KEY
        );

        try {
            // Log de l'URL
            System.out.println("URL générée : " + apiUrl);

            // Créer une connexion HTTP
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Vérifie le code HTTP
            int responseCode = connection.getResponseCode();
            System.out.println("Code de réponse : " + responseCode);

            if (responseCode != 200) {
                return "Erreur HTTP : " + responseCode;
            }

            // Lire la réponse
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Log de la réponse brute
            System.out.println("Réponse brute : " + response.toString());

            // Extraire les données JSON
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray results = jsonResponse.getAsJsonArray("results");

            if (results.size() > 0) {
                JsonObject firstResult = results.get(0).getAsJsonObject();

                // Variables pour stocker les informations extraites
                String country = null, city = null, street = null;

                // Analyser les composants d'adresse
                JsonArray addressComponents = firstResult.getAsJsonArray("address_components");
                for (int i = 0; i < addressComponents.size(); i++) {
                    JsonObject component = addressComponents.get(i).getAsJsonObject();
                    JsonArray types = component.getAsJsonArray("types");

                    // Recherche du pays
                    if (types.contains(new JsonPrimitive("country"))) {
                        country = component.get("long_name").getAsString();
                    }
                    // Recherche de la ville
                    else if (types.contains(new JsonPrimitive("locality")) || types.contains(new JsonPrimitive("administrative_area_level_1"))) {
                        city = component.get("long_name").getAsString();
                    }
                    // Recherche du nom de la rue
                    else if (types.contains(new JsonPrimitive("route"))) {
                        street = component.get("long_name").getAsString();
                    }
                }

                // Construire la réponse sous le format souhaité : country,city,street
                StringBuilder result = new StringBuilder();
                if (country != null) {
                    result.append(country);
                }
                if (city != null) {
                    if (result.length() > 0) result.append(",");
                    result.append(city);
                }
                if (street != null) {
                    if (result.length() > 0) result.append(",");
                    result.append(street);
                }

                return result.length() > 0 ? result.toString() : "Aucune adresse trouvée pour ces coordonnées.";
            } else {
                return "Aucune adresse trouvée pour ces coordonnées.";
            }

        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
            e.printStackTrace();
            return "Erreur lors de la récupération de l'adresse.";
        }
    }

        public static void main(String[] args) {
            try {
                // Example: Hassan Tower, Rabat, Morocco
                GPSCoordinates coordinates = new GPSCoordinates(33.553224, -7.612015
                );
                String location = getDetailedLocation(coordinates);
                System.out.println("Détails de l'emplacement : " + location);
            } catch (Exception e) {
                e.printStackTrace();
}
}
    }
