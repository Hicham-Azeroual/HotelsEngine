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
import javafx.concurrent.Task;
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
    private static ElasticsearchClient client;
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
    }

    public boolean createHotel(Hotel hotel) {
        try {
            // Créer une requête d'indexation
            IndexRequest<Hotel> request = IndexRequest.of(i -> i
                    .index("hotels") // Nom de l'index Elasticsearch
                    .document(hotel)  // Document à indexer
            );
            IndexResponse response = client.index(request);
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
    // Function to retrieve all hotels from Elasticsearch and display them in the console
    public static Task<List<Hotel>> retrieveAllHotels() {
        return new Task<>() {
            @Override
            protected List<Hotel> call() {
                try {
                    System.out.println(1);
                    List<Hotel> hotels = new ArrayList<>();
                    SearchRequest searchRequest = SearchRequest.of(s -> s
                            .index("hotels")
                            .size(3) // Limiter le nombre de résultats pour les tests
                    );

                    SearchResponse<Hotel> response = client.search(searchRequest, Hotel.class);

                    // Parcourir les résultats et mapper l'ID du document
                    for (Hit<Hotel> hit : response.hits().hits()) {
                        Hotel hotel = hit.source(); // Récupérer les données du document
                        hotel.setId(hit.id()); // Ajouter l'ID du document à l'objet Hotel
                        hotels.add(hotel);
                        System.out.println(22);
                    }

                    System.out.println("Hotels retrieved successfully: " + hotels.size());
                    return hotels;
                } catch (Exception e) {
                    System.err.println("Error during hotel retrieval: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        };
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
    // Method to perform aggregation query and return the results

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

    // Method to get the number of hotels with missing data
    public long getNumberOfHotelsWithMissingData() {
        try {
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();

            // Check for missing fields
            boolQuery.should(s -> s.bool(b -> b
                    .mustNot(m -> m.exists(ExistsQuery.of(e -> e.field("name"))))
                    .mustNot(m -> m.exists(ExistsQuery.of(e -> e.field("overall_rating"))))
                    .mustNot(m -> m.exists(ExistsQuery.of(e -> e.field("reviews"))))
            ));

            // Check for empty strings (only for text fields)
            boolQuery.should(s -> s.term(TermQuery.of(t -> t.field("name").value(""))));

            // Check for zero values (for numeric fields)
            boolQuery.should(s -> s.term(TermQuery.of(t -> t.field("overall_rating").value(0.0))));
            boolQuery.should(s -> s.term(TermQuery.of(t -> t.field("reviews").value(0))));

            CountRequest countRequest = CountRequest.of(c -> c
                    .index("hotels")
                    .query(boolQuery.build()._toQuery())
            );

            CountResponse response = client.count(countRequest);
            return response.count();
        } catch (IOException e) {
            System.err.println("Error getting number of hotels with missing data: " + e.getMessage());
            return 0;
        }
    }    // Method to get the total number of reviews
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
    public void closeClient() {
        ElasticsearchConnection.closeClient();
        System.out.println("Elasticsearch client closed.");
    }
}