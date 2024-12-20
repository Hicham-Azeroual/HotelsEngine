package org.example.hotelssearch.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.ExistsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonParser;
import javafx.scene.control.Alert;
import org.example.hotelssearch.models.Hotel;
import org.example.hotelssearch.utils.ElasticsearchConnection;
import org.example.hotelssearch.utils.GPSCoordinates;
import org.example.hotelssearch.utils.GeocodingService;
import org.example.hotelssearch.utils.RequestCounter;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import org.json.JSONObject;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.*;

public class HotelService {
    private static final String API_KEY = "cur_live_q4NCSYGhZqU5Ta99md9kmDdhuwVEVRB8SJP1Y9Y9";
    private static final String API_URL = "https://api.currencyapi.com/v3/latest?apikey="+API_KEY;

    private static ElasticsearchClient client = null;
    // Constructor to initialize the Elasticsearch client
    public HotelService() {
        // Ensure the Elasticsearch client is initialized before using it
        try {
            ElasticsearchConnection.initializeClient();  // Explicitly initialize the client
            this.client = ElasticsearchConnection.getClient();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Elasticsearch client", e);
}

}// Function to search hotels by name
    public static double convertCurrency(String targetCurrency, double amount) throws Exception {
        // Fetch the latest exchange rates
        JSONObject response = fetchExchangeRates();

        // Check if the target currency exists in the response
        if (response.getJSONObject("data").has(targetCurrency)) {
            // Get the exchange rate for the target currency
            double exchangeRate = response.getJSONObject("data").getJSONObject(targetCurrency).getDouble("value");

            // Perform the conversion
            return amount * exchangeRate;
        } else {
            throw new IllegalArgumentException("Target currency not found in the API response.");
        }
    }

    // Fetch the latest exchange rates from the API
    private static JSONObject fetchExchangeRates() throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Read the response
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Parse the JSON response
        return new JSONObject(response.toString());
}
    public long getTotalNumberOfUsers()
    {
        try {
            // Créer une requête de comptage pour l'index "users"
            CountRequest countRequest = CountRequest.of(c -> c
                    .index("users") // Spécifiez l'index "users"
            );

            // Exécuter la requête de comptage
            CountResponse response = client.count(countRequest);

            // Récupérer le nombre total de documents dans l'index "users"
            long totalUsers = response.count();

            // Afficher le résultat dans la console (facultatif)
            System.out.println("Total number of users: " + totalUsers);

            return totalUsers;

        } catch (IOException e) {
            System.err.println("Error retrieving total number of users: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve total number of users", e);
        }
    }
//    public List<Hotel> searchHotelsByName(String name) {
//        List<Hotel> matchingHotels = new ArrayList<>();
//
//        try {
//            // Create the search query
//            SearchRequest searchRequest = SearchRequest.of(s -> s
//                    .index("hotels") // Specify the index name
//                    .query(q -> q.match(m -> m
//                            .field("name") // Search in the "name" field
//                            .query(name)  // The search query
//                    ))
//            );
//
//            // Execute the search query
//            SearchResponse<Hotel> response = client.search(searchRequest, Hotel.class);
//
//            // Print the raw response for debugging
//            System.out.println("Elasticsearch Response: " + response.toString());
//
//            // Extract the hits and map them to Hotel objects
//            for (Hit<Hotel> hit : response.hits().hits()) {
//                matchingHotels.add(hit.source());
//            }
//
//            // Display the results in the console
//            if (matchingHotels.isEmpty()) {
//                System.out.println("No hotels found for the name: " + name);
//            } else {
//                System.out.println("Hotels matching the name \"" + name + "\":");
//                matchingHotels.forEach(System.out::println);
//            }
//
//        } catch (IOException e) {
//            System.err.println("Error searching hotels by name: " + e.getMessage());
//        }
//
//        return matchingHotels;
//    }    // Function to retrieve all hotels from Elasticsearch and display them in the console
    public List<Hotel> executeSearch(SearchRequest searchRequest) {
        try {
            SearchResponse<Hotel> response = client.search(searchRequest, Hotel.class);
            List<Hotel> hotels = new ArrayList<>();
            for (Hit<Hotel> hit : response.hits().hits()) {
                Hotel hotel = hit.source();
                if (hotel != null) {
                    hotel.set_id(hit.id());
                    hotels.add(hotel);
                }
            }
            return hotels;
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to execute search query.");
            return new ArrayList<>();
        }
    }
    public boolean createHotel(Hotel hotel) {
        try {
            // Convertir l'objet Hotel en un format compatible avec Elasticsearch
            Map<String, Object> hotelMap = hotel.toElasticsearchFormat();

            // Créer une requête d'indexation
            IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
                    .index("hotels") // Nom de l'index Elasticsearch
                    .document(hotelMap)  // Document à indexer
            );

            // Exécuter la requête d'indexation
            IndexResponse response = client.index(request);

            // Vérifier le résultat de la requête
            if (response.result() == Result.Created) {
                System.out.println("Hotel added successfully with ID: " + response.id());
                return true;
            } else {
                System.err.println("Failed to add hotel: " + response.result());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteHotelById(String hotelId) {
        try {
            // Vérifier si l'ID de l'hôtel est valide
            if (hotelId == null || hotelId.isEmpty()) {
                System.err.println("Error: Hotel ID cannot be null or empty.");
                return false;
            }

            // Créer la requête de suppression
            DeleteRequest deleteRequest = DeleteRequest.of(d -> d
                    .index("hotels") // Spécifier l'index
                    .id(hotelId)      // ID de l'hôtel à supprimer
            );

            // Exécuter la requête de suppression
            DeleteResponse response = client.delete(deleteRequest);

            // Vérifier le statut de la réponse
            if (response.result() == Result.Deleted) {
                System.out.println("Hotel with ID " + hotelId + " has been successfully deleted.");
                return true;
            } else {
                System.err.println("Failed to delete hotel with ID " + hotelId + ". Result: " + response.result());
                return false;
            }

        } catch (IOException e) {
            System.err.println("Error deleting hotel with ID " + hotelId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public Hotel retrieveHotelById(String hotelId) {
        try {
            // Créer une requête de recherche par ID
            GetRequest getRequest = GetRequest.of(g -> g
                    .index("hotels") // Nom de l'index
                    .id(hotelId)      // ID de l'hôtel
            );

            // Exécuter la requête
            GetResponse<Hotel> response = client.get(getRequest, Hotel.class);

            // Vérifier si le document existe
            if (response.found()) {
                // Récupérer l'objet Hotel
                Hotel hotel = response.source();

                // Assigner l'ID Elasticsearch à l'objet Hotel
                if (hotel != null) {
                    hotel.set_id(response.id());
                }

                return hotel;
            } else {
                System.err.println("Hotel with ID " + hotelId + " not found.");
                return null;
            }
        } catch (IOException e) {
            System.err.println("Error retrieving hotel with ID " + hotelId + ": " + e.getMessage());
            return null;
        }
    }

    public static List<Hotel> retrieveAllHotels(int from, int size) {
        try {
            RequestCounter.increment(); // Increment the request counter

            // Search query to retrieve hotels from the 'hotels' index with pagination
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("hotels")
                    .from(from)
                    .size(size)
            );

            SearchResponse<Hotel> response = client.search(searchRequest, Hotel.class);

            // Check if we have hits and process the results
            List<Hotel> hotels = new ArrayList<>();
            for (Hit<Hotel> hit : response.hits().hits()) {
                Hotel hotel = hit.source();
                if (hotel != null) {
                    // Set the _id field in the Hotel object
                    hotel.set_id(hit.id());
                    hotels.add(hotel);
                }
            }

            if (hotels.isEmpty()) {
                System.out.println("No hotels found.");
            } else {
                // Display each hotel in the console, including the _id
                for (Hotel hotel : hotels) {
                    System.out.println("ID: " + hotel.get_id() + ", " + hotel);
                }
            }
            return hotels;
        } catch (IOException e) {
            System.err.println("Error retrieving hotels: " + e.getMessage());
        }
        return new ArrayList<>();
}
    public static String buildRangeQuery(float minRating) throws Exception {
        // Use Jackson to construct the JSON query
        ObjectMapper objectMapper = new ObjectMapper();

        // Build query as a map
        Map<String, Object> rangeCondition = Map.of(
                "overall_rating", Map.of("gte", minRating)
        );
        Map<String, Object> rangeQuery = Map.of(
                "range", rangeCondition
        );

        // Convert map to JSON string
        return objectMapper.writeValueAsString(rangeQuery);
    }
    public static String buildRangePriceQuery(float maxPricing) throws Exception {
        // Use Jackson to construct the JSON query
        ObjectMapper objectMapper = new ObjectMapper();

        // Build the query as a map
        Map<String, Object> rangeCondition = Map.of(
                "lowest_rate", Map.of("lte", maxPricing) // Specify "lte" (less than or equal)
        );
        Map<String, Object> rangeQuery = Map.of(
                "range", rangeCondition
        );

        // Convert the map to a JSON string
        return objectMapper.writeValueAsString(rangeQuery);
    }
    public static String buildGeoQuery(double lat, double lon) throws Exception {
        // Use Jackson to construct the JSON query
        ObjectMapper objectMapper = new ObjectMapper();

        // Build the query as a map without distance (just using the gps_coordinates)
        Map<String, Object> geoCondition = Map.of(
                "lat", lat,   // Latitude
                "lon", lon    // Longitude
        );

        Map<String, Object> geoQuery = Map.of(
                "geo_distance", Map.of(
                        "distance", "100km", // You can adjust the distance as needed
                        "gps_coordinates", geoCondition
                )
        );

        // Convert the map to a JSON string
        return objectMapper.writeValueAsString(geoQuery);
    }
    public List<Hotel> searchHotelsByLocation(double lat, double lon) {
        List<Hotel> matchingHotels = new ArrayList<>(); // To store matching results

        try {
            // Build the geo-query without the distance
            String queryJson = buildGeoQuery(lat, lon);
            System.out.println("Generated query: " + queryJson);

            // Parse the JSON query into a Query object for Elasticsearch
            JsonReader jsonReader = Json.createReader(new StringReader(queryJson));
            JsonObject queryJsonObject = jsonReader.readObject();

            JsonParser parser = Json.createParser(new StringReader(queryJsonObject.toString()));
            Query query = Query.of(builder -> builder.withJson(parser, client._transport().jsonpMapper()));

            // Construct the search request
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("hotels") // Specify the Elasticsearch index
                    .query(query)    // Attach the geo-query
            );

            // Execute the search request and parse the response
            SearchResponse<Hotel> response = client.search(searchRequest, Hotel.class);

            // Map hits to Hotel objects
            for (Hit<Hotel> hit : response.hits().hits()) {
                matchingHotels.add(hit.source());
            }

            // Display the results
            if (matchingHotels.isEmpty()) {
                System.out.println("No hotels found near location (" + lat + ", " + lon + ").");
            } else {
                System.out.println("Hotels found near location (" + lat + ", " + lon + "):");
                matchingHotels.forEach(System.out::println);
            }
        } catch (IOException e) {
            System.err.println("Error searching hotels by location: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error building query: " + e.getMessage());
        }

        return matchingHotels;
    }
//    public void searchByCityName(String cityName) {
//        try {
//            // Step 1: Retrieve GPS coordinates for the city
//            System.out.println("Fetching GPS coordinates for city: " + cityName);
//            GPSCoordinates coordinates = GeocodingService.getLocationCoordinates(cityName);
//
//            // Step 2: Extract latitude and longitude from the coordinates
//            double latitude = coordinates.getLatitude();
//            double longitude = coordinates.getLongitude();
//            System.out.println("Coordinates for " + cityName + ": " + coordinates);
//
//            // Step 3: Search for hotels using the coordinates
//            List<Hotel> hotels = searchHotelsByLocation(latitude, longitude);
//
//            // Step 4: Display the search results
//            if (hotels.isEmpty()) {
//                System.out.println("No hotels found near " + cityName + ".");
//            } else {
//                System.out.println("Hotels found near " + cityName + ":");
//                hotels.forEach(System.out::println);
//            }
//        } catch (Exception e) {
//            System.err.println("Error searching for hotels: " + e.getMessage());
//        }
//    }
    // Add this function to your HotelService class
//    public List<Hotel> searchHotelsByRating(float minRating) {
//        List<Hotel> matchingHotels = new ArrayList<>();
//
//        try {
//            // Build the range query for `overall_rating`
//            String queryJson = buildRangeQuery(minRating);
//
//            // Parse the JSON query into a Query object using JsonParser
//            JsonpMapper jsonpMapper = client._transport().jsonpMapper();
//            JsonReader jsonReader = Json.createReader(new StringReader(queryJson));
//            JsonObject queryJsonObject = jsonReader.readObject();
//            JsonParser parser = Json.createParser(new StringReader(queryJsonObject.toString()));
//            Query query = Query.of(builder -> builder.withJson(parser, jsonpMapper));
//
//            // Create and execute the search request
//            SearchRequest searchRequest = SearchRequest.of(s -> s
//                    .index("hotels")
//                    .query(query)  // Attach the range query
//            );
//
//            SearchResponse<Hotel> response = client.search(searchRequest, Hotel.class);
//
//            // Extract and map hits to Hotel objects
//            for (Hit<Hotel> hit : response.hits().hits()) {
//                matchingHotels.add(hit.source());
//            }
//
//            // Display the results
//            if (matchingHotels.isEmpty()) {
//                System.out.println("No hotels found with a rating greater than or equal to " + minRating);
//            } else {
//                System.out.println("Hotels with a rating >= " + minRating + ":");
//                matchingHotels.forEach(System.out::println);
//            }
//
//        } catch (IOException e) {
//            System.err.println("Error searching hotels by rating: " + e.getMessage());
//        } catch (Exception e) {
//            System.err.println("Error building query: " + e.getMessage());
//        }
//
//        return matchingHotels;
//    }
//    public List<Hotel> searchHotelsByMaxPrice(float maxPrice) {
//        List<Hotel> matchingHotels = new ArrayList<>();
//
//        try {
//            // Build the range query for the price
//            String queryJson = buildRangePriceQuery(maxPrice);
//
//            // Parse the JSON query into a Query object
//            JsonReader jsonReader = Json.createReader(new StringReader(queryJson));
//            JsonObject queryJsonObject = jsonReader.readObject();
//            JsonParser parser = Json.createParser(new StringReader(queryJsonObject.toString()));
//            Query query = Query.of(builder -> builder.withJson(parser, client._transport().jsonpMapper()));
//
//            // Create and execute the search request
//            SearchRequest searchRequest = SearchRequest.of(s -> s
//                    .index("hotels")
//                    .query(query) // Attach the range query
//            );
//
//            SearchResponse<Hotel> response = client.search(searchRequest, Hotel.class);
//
//            // Map hits to Hotel objects
//            for (Hit<Hotel> hit : response.hits().hits()) {
//                matchingHotels.add(hit.source());
//            }
//
//            // Display the results
//            if (matchingHotels.isEmpty()) {
//                System.out.println("No hotels found with a price <= " + maxPrice);
//            } else {
//                System.out.println("Hotels with a price <= " + maxPrice + ":");
//                matchingHotels.forEach(System.out::println);
//            }
//
//        } catch (IOException e) {
//            System.err.println("Error searching hotels by maximum price: " + e.getMessage());
//        } catch (Exception e) {
//            System.err.println("Error building query: " + e.getMessage());
//        }
//
//        return matchingHotels;
//    }

    public static String buildQuery(String searchTerm, Float minRating, Float maxPrice, Double latitude, Double longitude, Double radiusInKm, List<String> amenities, int from, int size) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        // List to hold all the must queries
        List<Map<String, Object>> mustQueries = new ArrayList<>();

        // Match query for hotel name with improved parameters
        if (searchTerm != null && !searchTerm.isEmpty()) {
            Map<String, Object> matchQuery = Map.of(
                    "match", Map.of(
                            "name", Map.of(
                                    "query", searchTerm,
                                    "fuzziness", "AUTO",
                                    "prefix_length", 2,
                                    "operator", "and"
                            )
                    )
            );
            mustQueries.add(matchQuery);
        }

        // Range query for rating
        if (minRating != null) {
            Map<String, Object> ratingRangeQuery = Map.of(
                    "range", Map.of(
                            "overall_rating", Map.of("gte", minRating)
                    )
            );
            mustQueries.add(ratingRangeQuery);
        }

        // Range query for price
        if (maxPrice != null && maxPrice > 0) {
            Map<String, Object> priceRangeQuery = Map.of(
                    "range", Map.of(
                            "lowest_rate", Map.of("lte", maxPrice)
                    )
            );
            mustQueries.add(priceRangeQuery);
        }

        // Bool query for amenities with fuzziness
        if (amenities != null && !amenities.isEmpty()) {
            List<Map<String, Object>> shouldQueries = new ArrayList<>();
            for (String amenity : amenities) {
                Map<String, Object> amenitiesQuery = Map.of(
                        "match", Map.of(
                                "amenities", Map.of(
                                        "query", amenity,
                                        "fuzziness", "AUTO"
                                )
                        )
                );
                shouldQueries.add(amenitiesQuery);
            }
            Map<String, Object> amenitiesBoolQuery = Map.of("bool", Map.of("should", shouldQueries));
            mustQueries.add(amenitiesBoolQuery);
        }

        // List to hold all the filter queries
        List<Map<String, Object>> filterQueries = new ArrayList<>();

        // Geo distance query
        if (latitude != 0 && longitude != 0 && radiusInKm != null) {
            Map<String, Object> geoDistanceQuery = Map.of(
                    "geo_distance", Map.of(
                            "distance", radiusInKm + "km",
                            "gps_coordinates", Map.of("lat", latitude, "lon", longitude)
                    )
            );
            filterQueries.add(geoDistanceQuery);
        }

        // Combine all queries into a bool query
        Map<String, Object> boolQuery = new HashMap<>();
        if (!mustQueries.isEmpty()) {
            boolQuery.put("must", mustQueries);
        }
        if (!filterQueries.isEmpty()) {
            boolQuery.put("filter", filterQueries);
        }

        // Wrap the bool query in the "query" key
        Map<String, Object> queryWrapper = Map.of(
                "query", Map.of("bool", boolQuery),
                "from", from,
                "size", size
        );

        // Serialize to JSON string
        return objectMapper.writeValueAsString(queryWrapper);
    }
    public List<Hotel> executeQuery(String searchTerm, Float minRating, Float maxPrice, String cityName, Double radiusInKm, List<String> amenities, int from, int size, GPSCoordinates gpsCoordinates) throws Exception {
        // Get GPS coordinates for the city
        GPSCoordinates coordinates = null;
        double latitude = 0;
        double longitude = 0;
        if (cityName != null && !cityName.isEmpty()) {
            coordinates = GeocodingService.getLocationCoordinates(cityName);
            latitude = coordinates.getLatitude();
            longitude = coordinates.getLongitude();
            radiusInKm = 70.0;
        } else if (gpsCoordinates != null) {
            latitude = gpsCoordinates.getLatitude();
            longitude = gpsCoordinates.getLongitude();
        } else {
            System.out.println("City name is not provided or is empty.");
        }

        // Check if coordinates were successfully retrieved
        if (coordinates == null && gpsCoordinates == null) {
            System.out.print("Could not retrieve coordinates for the city: " + cityName);
        }

        // Build the query
        String queryJson = buildQuery(searchTerm, minRating, maxPrice, latitude, longitude, radiusInKm, amenities, from, size);

        // Create the search request
        SearchRequest searchRequest = SearchRequest.of(sr -> sr
                .index("hotels") // Replace with your index name
                .withJson(new StringReader(queryJson))
        );

        // Execute the search
        SearchResponse<Hotel> response = client.search(searchRequest, Hotel.class);

        // Print results
        List<Hotel> hotels = new ArrayList<>();
        response.hits().hits().forEach(hit -> hotels.add(hit.source()));

        // Display the results directly
        if (hotels.isEmpty()) {
            System.out.println("No hotels found matching the criteria.");
        } //else {
        // System.out.println("Hotels found:");
        //for (Hotel hotel : hotels) {
        //  System.out.println(hotel);
        //}
        //}
        return hotels;
    }
    public static String buildRatingDistributionQuery() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Build the aggregation query
        Map<String, Object> rangeAggregation = Map.of(
                "field", "overall_rating",
                "ranges", List.of(
                        Map.of("key", "1-2", "to", 2.0),
                        Map.of("key", "2-3", "from", 2.0, "to", 3.0),
                        Map.of("key", "3-4", "from", 3.0, "to", 4.0),
                        Map.of("key", "4-5", "from", 4.0, "to", 5.0)
                )
        );

        Map<String, Object> aggregation = Map.of(
                "rating_ranges", Map.of("range", rangeAggregation)
        );

        // Add a filter to include only documents that have a rating (field "overall_rating" exists)
        Map<String, Object> query = Map.of(
                "size", 0,
                "query", Map.of(
                        "exists", Map.of("field", "overall_rating")  // Filter out documents without a rating
                ),
                "aggs", aggregation
        );

        // Convert map to JSON string
        return objectMapper.writeValueAsString(query);
    }

    // Method to perform aggregation query and return the results
    public Map<String, Double> getRatingDistribution() {
        Map<String, Double> ratingDistribution = new HashMap<>();

        try {
            // Build the aggregation query
            String queryJson = buildRatingDistributionQuery();

            // Parse the JSON query into a SearchRequest object
            JsonReader jsonReader = Json.createReader(new StringReader(queryJson));
            JsonObject queryJsonObject = jsonReader.readObject();
            JsonParser parser = Json.createParser(new StringReader(queryJsonObject.toString()));
            SearchRequest searchRequest = SearchRequest.of(builder -> builder.withJson(parser, client._transport().jsonpMapper()));

            // Execute the search query
            SearchResponse<Void> response = client.search(searchRequest, Void.class);

            // Extract the aggregation results
            var ratingRanges = response.aggregations().get("rating_ranges").range().buckets().array();

            // Calculate the total number of hotels by summing the doc_count values
            long totalHotels = 0;
            for (var bucket : ratingRanges) {
                long docCount = bucket.docCount();
                totalHotels += docCount;
            }

            // Now calculate the percentage for each bucket
            for (var bucket : ratingRanges) {
                String key = bucket.key();
                long docCount = bucket.docCount();
                double percentage = (docCount * 100.0) / totalHotels;
                ratingDistribution.put(key, percentage);
            }

            // Display the results in the console
            System.out.println("Rating Distribution:");
            ratingDistribution.forEach((key, percentage) -> System.out.println(key + ": " + percentage + "%"));

        } catch (IOException e) {
            System.err.println("Error performing aggregation query: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error building query: " + e.getMessage());
        }

        return ratingDistribution;
    }
    public long getTotalNumberOfHotels() {
        try {
            CountRequest countRequest = CountRequest.of(c -> c
                    .index("hotels")
            );

            CountResponse response = client.count(countRequest);
            return response.count();
        } catch (IOException e) {
            System.err.println("Error getting total number of hotels: " + e.getMessage());
            return 0;
        }
    }

//    // Method to get the number of hotels with missing data
//    public long getNumberOfHotelsWithMissingData() {
//        try {
//            BoolQuery.Builder boolQuery = new BoolQuery.Builder();
//
//            // Check for missing fields
//            boolQuery.should(s -> s.bool(b -> b
//                    .mustNot(m -> m.exists(ExistsQuery.of(e -> e.field("name"))))
//                    .mustNot(m -> m.exists(ExistsQuery.of(e -> e.field("overall_rating"))))
//                    .mustNot(m -> m.exists(ExistsQuery.of(e -> e.field("reviews"))))
//            ));
//
//            // Check for empty strings (only for text fields)
//            boolQuery.should(s -> s.term(TermQuery.of(t -> t.field("name").value(""))));
//
//            // Check for zero values (for numeric fields)
//            boolQuery.should(s -> s.term(TermQuery.of(t -> t.field("overall_rating").value(0.0))));
//            boolQuery.should(s -> s.term(TermQuery.of(t -> t.field("reviews").value(0))));
//
//            CountRequest countRequest = CountRequest.of(c -> c
//                    .index("hotels")
//                    .query(boolQuery.build()._toQuery())
//            );
//
//            CountResponse response = client.count(countRequest);
//            return response.count();
//        } catch (IOException e) {
//            System.err.println("Error getting number of hotels with missing data: " + e.getMessage());
//            return 0;
//        }
//    }    // Method to get the total number of reviews
    public long getTotalNumberOfReviews() {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("hotels")
                    .size(0)
                    .aggregations("total_reviews", a -> a
                            .sum(q -> q.field("reviews"))
                    )
            );

            SearchResponse<Void> response = client.search(searchRequest, Void.class);
            return (long) response.aggregations().get("total_reviews").sum().value();
        } catch (IOException e) {
            System.err.println("Error getting total number of reviews: " + e.getMessage());
            return 0;
        }
    }
    public int retrieveNumberOfRequests() {
        return RequestCounter.getRequestCount();
    }
    public List<Hotel> sortHotels(List<Hotel> hotels, String sortOption) {
        switch (sortOption) {
            case "Price":
                hotels.sort(Comparator.comparingDouble(Hotel::getLowest_rate));
                break;
            case "Name":
                hotels.sort(Comparator.comparing(Hotel::getName));
                break;
            //case "Distance":
                // Assuming you have a method to calculate distance
              //  hotels.sort(Comparator.comparingDouble(Hotel::getDistance));
              //  break;
            case "Rating":
                hotels.sort(Comparator.comparingDouble(Hotel::getOverall_rating).reversed());
                break;
            default:
                // Default sorting by rating
                hotels.sort(Comparator.comparingDouble(Hotel::getOverall_rating).reversed());
                break;
        }
        return hotels;
    }

    public static String buildAutocompleteQuery(String searchTerm, int from, int size) throws Exception {
        // Initialize the ObjectMapper for JSON serialization
        ObjectMapper objectMapper = new ObjectMapper();

        // List to hold all the must queries
        List<Map<String, Object>> mustQueries = new ArrayList<>();

        // Autocomplete query for hotel name using prefix match
        if (searchTerm != null && !searchTerm.isEmpty()) {
            Map<String, Object> matchQuery = Map.of(
                    "match", Map.of(
                            "name", Map.of(
                                    "query", searchTerm,
                                    "operator", "and"
                            )
                    )
            );
            mustQueries.add(matchQuery);
        }

        // Wrap the query inside a "query" field
        Map<String, Object> boolQuery = new HashMap<>();
        if (!mustQueries.isEmpty()) {
            boolQuery.put("must", mustQueries);
        }

        // Final query wrapper with pagination
        Map<String, Object> queryWrapper = Map.of(
                "query", Map.of("bool", boolQuery),
                "from", from,
                "size", size
        );

        // Convert to JSON string
        return objectMapper.writeValueAsString(queryWrapper);
    }
    public List<String> executeAutocompleteQuery(String searchTerm, int from, int size) throws Exception {
        // Build the autocomplete query
        String queryJson = buildAutocompleteQuery(searchTerm, from, size);

        // Create the search request
        SearchRequest searchRequest = SearchRequest.of(sr -> sr
                .index("hotels") // Replace with your actual index name
                .withJson(new StringReader(queryJson))
        );

        // Execute the search query
        SearchResponse<Map> response = client.search(searchRequest, Map.class);

        // Extract the results from the response
        List<String> hotelNames = new ArrayList<>();
        response.hits().hits().forEach(hit -> {
            // Extract the name of the hotel (or whatever field you're searching for)
            hotelNames.add((String) hit.source().get("name"));
        });

        // Return the list of hotel names (or whatever field you want)
        return hotelNames;
    }

    public boolean updateHotel(String hotelId, Hotel updatedHotel) {
        try {
            // Convertir l'objet Hotel en un format compatible avec Elasticsearch
            Map<String, Object> hotelMap = updatedHotel.toElasticsearchFormat();

            // Créer la requête de mise à jour
            UpdateRequest<Map<String, Object>, Map<String, Object>> updateRequest = UpdateRequest.of(u -> u
                    .index("hotels") // Nom de l'index
                    .id(hotelId)      // ID de l'hôtel
                    .doc(hotelMap)    // Document mis à jour
                    .docAsUpsert(true) // Si le document n'existe pas, il sera créé
            );

            // Exécuter la requête de mise à jour
            UpdateResponse<Map<String, Object>> response = client.update(updateRequest, Map.class);

            // Vérifier le statut de la réponse
            if (response.result() == Result.Updated || response.result() == Result.Created) {
                System.out.println("Hotel with ID " + hotelId + " has been successfully updated.");
                return true;
            } else {
                System.err.println("Failed to update hotel with ID " + hotelId + ". Result: " + response.result());
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error updating hotel with ID " + hotelId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void closeClient() {
        ElasticsearchConnection.closeClient();
        System.out.println("Elasticsearch client closed.");
    }

}