package org.example.hotelssearch.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonParser;
import org.example.hotelssearch.models.Hotel;
import org.example.hotelssearch.utils.ElasticsearchConnection;
import org.example.hotelssearch.utils.GPSCoordinates;
import org.example.hotelssearch.utils.GeocodingService;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HotelService {
    private final ElasticsearchClient client;
    // Constructor to initialize the Elasticsearch client
    public HotelService() {
        // Ensure the Elasticsearch client is initialized before using it
        try {
            ElasticsearchConnection.initializeClient();  // Explicitly initialize the client
            this.client = ElasticsearchConnection.getClient();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Elasticsearch client", e);
        }
    }
    // Function to search hotels by name
    public List<Hotel> searchHotelsByName(String name) {
        List<Hotel> matchingHotels = new ArrayList<>();

        try {
            // Create the search query
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("hotels") // Specify the index name
                    .query(q -> q.match(m -> m
                            .field("name") // Search in the "name" field
                            .query(name)  // The search query
                    ))
            );

            // Execute the search query
            SearchResponse<Hotel> response = client.search(searchRequest, Hotel.class);

            // Print the raw response for debugging
            System.out.println("Elasticsearch Response: " + response.toString());

            // Extract the hits and map them to Hotel objects
            for (Hit<Hotel> hit : response.hits().hits()) {
                matchingHotels.add(hit.source());
            }

            // Display the results in the console
            if (matchingHotels.isEmpty()) {
                System.out.println("No hotels found for the name: " + name);
            } else {
                System.out.println("Hotels matching the name \"" + name + "\":");
                matchingHotels.forEach(System.out::println);
            }

        } catch (IOException e) {
            System.err.println("Error searching hotels by name: " + e.getMessage());
        }

        return matchingHotels;
    }    // Function to retrieve all hotels from Elasticsearch and display them in the console
    public void retrieveAllHotels() {
        try {
            // Search query to retrieve all hotels from the 'hotels' index
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("hotels")
                    .size(300)  // Retrieve a maximum of 300 documents (adjust as needed)
            );

            SearchResponse<Hotel> response = client.search(searchRequest, Hotel.class);

            // Check if we have hits and process the results
            List<Hotel> hotels = response.hits().hits().stream()
                    .map(Hit::source)  // Extract hotel objects from the hits
                    .toList();

            if (hotels.isEmpty()) {
                System.out.println("No hotels found.");
            } else {
                // Display each hotel in the console
                for (Hotel hotel : hotels) {
                    System.out.println(hotel);
                }
            }

        } catch (IOException e) {
            System.err.println("Error retrieving hotels: " + e.getMessage());
        }
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
    public void searchByCityName(String cityName) {
        try {
            // Step 1: Retrieve GPS coordinates for the city
            System.out.println("Fetching GPS coordinates for city: " + cityName);
            GPSCoordinates coordinates = GeocodingService.getLocationCoordinates(cityName);

            // Step 2: Extract latitude and longitude from the coordinates
            double latitude = coordinates.getLatitude();
            double longitude = coordinates.getLongitude();
            System.out.println("Coordinates for " + cityName + ": " + coordinates);

            // Step 3: Search for hotels using the coordinates
            List<Hotel> hotels = searchHotelsByLocation(latitude, longitude);

            // Step 4: Display the search results
            if (hotels.isEmpty()) {
                System.out.println("No hotels found near " + cityName + ".");
            } else {
                System.out.println("Hotels found near " + cityName + ":");
                hotels.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.err.println("Error searching for hotels: " + e.getMessage());
        }
    }
    // Add this function to your HotelService class
    public List<Hotel> searchHotelsByRating(float minRating) {
        List<Hotel> matchingHotels = new ArrayList<>();

        try {
            // Build the range query for `overall_rating`
            String queryJson = buildRangeQuery(minRating);

            // Parse the JSON query into a Query object using JsonParser
            JsonpMapper jsonpMapper = client._transport().jsonpMapper();
            JsonReader jsonReader = Json.createReader(new StringReader(queryJson));
            JsonObject queryJsonObject = jsonReader.readObject();
            JsonParser parser = Json.createParser(new StringReader(queryJsonObject.toString()));
            Query query = Query.of(builder -> builder.withJson(parser, jsonpMapper));

            // Create and execute the search request
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("hotels")
                    .query(query)  // Attach the range query
            );

            SearchResponse<Hotel> response = client.search(searchRequest, Hotel.class);

            // Extract and map hits to Hotel objects
            for (Hit<Hotel> hit : response.hits().hits()) {
                matchingHotels.add(hit.source());
            }

            // Display the results
            if (matchingHotels.isEmpty()) {
                System.out.println("No hotels found with a rating greater than or equal to " + minRating);
            } else {
                System.out.println("Hotels with a rating >= " + minRating + ":");
                matchingHotels.forEach(System.out::println);
            }

        } catch (IOException e) {
            System.err.println("Error searching hotels by rating: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error building query: " + e.getMessage());
        }

        return matchingHotels;
    }
    public List<Hotel> searchHotelsByMaxPrice(float maxPrice) {
        List<Hotel> matchingHotels = new ArrayList<>();

        try {
            // Build the range query for the price
            String queryJson = buildRangePriceQuery(maxPrice);

            // Parse the JSON query into a Query object
            JsonReader jsonReader = Json.createReader(new StringReader(queryJson));
            JsonObject queryJsonObject = jsonReader.readObject();
            JsonParser parser = Json.createParser(new StringReader(queryJsonObject.toString()));
            Query query = Query.of(builder -> builder.withJson(parser, client._transport().jsonpMapper()));

            // Create and execute the search request
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("hotels")
                    .query(query) // Attach the range query
            );

            SearchResponse<Hotel> response = client.search(searchRequest, Hotel.class);

            // Map hits to Hotel objects
            for (Hit<Hotel> hit : response.hits().hits()) {
                matchingHotels.add(hit.source());
            }

            // Display the results
            if (matchingHotels.isEmpty()) {
                System.out.println("No hotels found with a price <= " + maxPrice);
            } else {
                System.out.println("Hotels with a price <= " + maxPrice + ":");
                matchingHotels.forEach(System.out::println);
            }

        } catch (IOException e) {
            System.err.println("Error searching hotels by maximum price: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error building query: " + e.getMessage());
        }

        return matchingHotels;
    }






    public static String buildGlobalQuery(String searchTerm, float minRating, float maxPrice) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Build individual query components
        Map<String, Object> matchCondition = Map.of(
                "name", searchTerm,   // Match on hotel name
                "description", searchTerm // Optional: Match on description
        );
        Map<String, Object> rangeConditionRating = Map.of(
                "overall_rating", Map.of("gte", minRating)
        );
        Map<String, Object> rangeConditionPrice = Map.of(
                "lowest_rate", Map.of("lte", maxPrice)
        );

        // Combine them using a bool query
        Map<String, Object> boolQuery = Map.of(
                "must", List.of(
                        Map.of("match", matchCondition),
                        Map.of("range", rangeConditionRating),
                        Map.of("range", rangeConditionPrice)

                )
        );

        Map<String, Object> query = Map.of(
                "bool", boolQuery
        );

        return objectMapper.writeValueAsString(query);
    }

    // Unified search function
    public List<Hotel> searchHotels(String searchTerm, float minRating, float maxPrice) {
        List<Hotel> matchingHotels = new ArrayList<>();

        try {
            // Generate the query JSON
            String queryJson = buildGlobalQuery(searchTerm, minRating, maxPrice);
            System.out.println("Generated query: " + queryJson);

            // Parse JSON into Elasticsearch Query object
            JsonReader jsonReader = Json.createReader(new StringReader(queryJson));
            JsonObject queryJsonObject = jsonReader.readObject();
            JsonParser parser = Json.createParser(new StringReader(queryJsonObject.toString()));
            Query query = Query.of(builder -> builder.withJson(parser, client._transport().jsonpMapper()));

            // Execute the search request
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("hotels")
                    .query(query)
            );

            SearchResponse<Hotel> response = client.search(searchRequest, Hotel.class);

            // Extract hits and map to Hotel objects
            for (Hit<Hotel> hit : response.hits().hits()) {
                matchingHotels.add(hit.source());
            }

            // Display results
            if (matchingHotels.isEmpty()) {
                System.out.println("No hotels match your search criteria.");
            } else {
                System.out.println("Matching hotels:");
                matchingHotels.forEach(System.out::println);
            }
        } catch (IOException e) {
            System.err.println("Error searching hotels: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error building query: " + e.getMessage());
        }

        return matchingHotels;
    }




    public static String buildQuery(String searchTerm, Float minRating, Float maxPrice, Double latitude, Double longitude, Double radiusInKm, List<String> amenities) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // List to hold all the must queries
        List<Map<String, Object>> mustQueries = new ArrayList<>();

        // Match query for hotel name
        if (searchTerm != null && !searchTerm.isEmpty()) {
            Map<String, Object> matchQuery = Map.of(
                    "match", Map.of("name", searchTerm) // Replace "name" with your field name if different
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
        if (maxPrice != null) {
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
        if (latitude != null && longitude != null && radiusInKm != null) {
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
        Map<String, Object> queryWrapper = Map.of("query", Map.of("bool", boolQuery),"size",100);

        // Serialize to JSON string
        return objectMapper.writeValueAsString(queryWrapper);
    }

    public void executeQuery(String searchTerm, Float minRating, Float maxPrice, String cityName, Double radiusInKm, List<String> amenities) throws Exception {
        // Get GPS coordinates for the city
        GPSCoordinates coordinates = GeocodingService.getLocationCoordinates(cityName);

        // Check if coordinates were successfully retrieved
        if (coordinates == null) {
            throw new Exception("Could not retrieve coordinates for the city: " + cityName);
        }

        // Build the query
        String queryJson = buildQuery(searchTerm, minRating, maxPrice, coordinates.getLatitude(), coordinates.getLongitude(), radiusInKm, amenities);

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
        } else {
            System.out.println("Hotels found:");
            for (Hotel hotel : hotels) {
                System.out.println(hotel);
            }
        }
    }    // Close the Elasticsearch client connection
    public void closeClient() {
        ElasticsearchConnection.closeClient();
        System.out.println("Elasticsearch client closed.");
    }
}