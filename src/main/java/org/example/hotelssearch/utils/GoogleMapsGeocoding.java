package org.example.hotelssearch.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class GoogleMapsGeocoding {

    private static final String API_KEY = "AIzaSyBKlP0OtG_BidvHX49sV1NSBwV659xqauo";

    public static String getDetailedLocation(GPSCoordinates gpsCoordinates) throws Exception {
        String apiUrl = String.format(Locale.US,
                "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s",
                gpsCoordinates.getLatitude(), gpsCoordinates.getLongitude(), API_KEY
        );

        try {
            // Log de l'URL

            // Créer une connexion HTTP
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Vérifie le code HTTP
            int responseCode = connection.getResponseCode();

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

            // Extraire les données JSON
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray results = jsonResponse.getAsJsonArray("results");

            if (results.size() > 0) {
                JsonObject firstResult = results.get(0).getAsJsonObject();
                String formattedAddress = firstResult.get("formatted_address").getAsString();

                // Variables pour stocker les informations extraites
                String country = null, city = null, district = null, streetName = null, streetNumber = null, apartmentNumber = null;

                // Analyser les composants d'adresse
                JsonArray addressComponents = firstResult.getAsJsonArray("address_components");
                for (int i = 0; i < addressComponents.size(); i++) {
                    JsonObject component = addressComponents.get(i).getAsJsonObject();
                    JsonArray types = component.getAsJsonArray("types");

                    // Recherche du pays
                    if (types.toString().contains("\"country\"")) {
                        country = component.get("long_name").getAsString();
                    }
                    // Recherche de la ville
                    else if (types.toString().contains("\"locality\"")) {
                        city = component.get("long_name").getAsString();
                    }
                    // Recherche de la province ou région administrative
                    else if (types.toString().contains("\"administrative_area_level_1\"")) {
                        city = component.get("long_name").getAsString();
                    }
                    // Recherche des sous-localités (quartiers)
                    else if (types.toString().contains("\"sublocality\"") || types.toString().contains("\"neighborhood\"")) {
                        district = component.get("long_name").getAsString();
                    }
                    // Recherche du nom de la rue
                    else if (types.toString().contains("\"route\"")) {
                        streetName = component.get("long_name").getAsString();
                    }
                    // Recherche du numéro de la rue
                    else if (types.toString().contains("\"street_number\"")) {
                        streetNumber = component.get("long_name").getAsString();
                    }
                    // Recherche du numéro de l'appartement
                    else if (types.toString().contains("\"subpremise\"")) {
                        apartmentNumber = component.get("long_name").getAsString();
                    }
                }

                // Construire la réponse sous le format souhaité
                StringBuilder result = new StringBuilder();
                if (formattedAddress != null) {
                    result.append(formattedAddress);
                }
                if (streetNumber != null && streetName != null) {
                    result.append(", ").append(streetNumber).append(" ").append(streetName);
                } else if (streetName != null) {
                    result.append(", ").append(streetName);
                }
                if (apartmentNumber != null) {
                    result.append(", Apt ").append(apartmentNumber);
                }
                if (district != null) {
                    result.append(", ").append(district);
                }
                if (city != null) {
                    result.append(", ").append(city);
                }
                if (country != null) {
                    result.append(", ").append(country);
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

    public static double calculateDistance(GPSCoordinates point1, GPSCoordinates point2) {
        final int R = 6371; // Radius of the earth in kilometers

        double latDistance = Math.toRadians(point2.getLatitude() - point1.getLatitude());
        double lonDistance = Math.toRadians(point2.getLongitude() - point1.getLongitude());

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(point1.getLatitude())) * Math.cos(Math.toRadians(point2.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in kilometers
    }
}