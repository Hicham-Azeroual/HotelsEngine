package org.example.hotelssearch.controllers;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.nodes.NodesStatsRequest;
import co.elastic.clients.elasticsearch.nodes.NodesStatsResponse;
import org.example.hotelssearch.utils.ElasticsearchConnection;
import org.example.hotelssearch.views.RequestChartApp;

import java.util.List;
import java.util.Random;

public class HotelRequestSimulator {
    private static volatile boolean running = false; // Flag to control the loop
    private static int requestCount = 0; // Track the total request count

    public static void main(String[] args) {
        // Add a shutdown hook to handle graceful termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            running = false;
        }));

        try {
            ElasticsearchConnection.initializeClient();
            ElasticsearchClient client = ElasticsearchConnection.getClient();

            // Start the JavaFX application
            new Thread(() -> RequestChartApp.main(args)).start();

            // Wait for the simulation to start
            while (!running) {
                Thread.sleep(100);
            }

            simulateInfiniteHotelRequests(client);

            ElasticsearchConnection.closeClient();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void startSimulation() {
        running = true;
    }

    public static void stopSimulation() {
        running = false;
    }

    public static void simulateInfiniteHotelRequests(ElasticsearchClient client) {
        Random random = new Random();
        System.out.println("Starting infinite request simulation. Use Ctrl+C to terminate.");

        while (running) {
            try {
                int requestsInBatch = 1 + random.nextInt(20); // Random number of requests
                System.out.println("Sending " + requestsInBatch + " requests.");

                for (int request = 1; request <= requestsInBatch && running; request++) {
                    String randomAmenity = getRandomAmenity(random);
                    float minRating = random.nextFloat() * 5;

                    SearchRequest searchRequest = SearchRequest.of(s -> s
                            .index("hotels")
                            .query(q -> q
                                    .bool(b -> b
                                            .must(m -> m
                                                    .match(mt -> mt
                                                            .field("overall_rating")
                                                            .query(String.valueOf(minRating))
                                                    )
                                            )
                                            .filter(f -> f
                                                    .match(mt -> mt
                                                            .field("amenities")
                                                            .query(randomAmenity)
                                                    )
                                            )
                                    )
                            )
                    );

                    SearchResponse<Object> response = client.search(searchRequest, Object.class);
                    List<Hit<Object>> hits = response.hits().hits();

                    if (hits.isEmpty()) {
                        System.out.println("Request " + request + ": No results found.");
                    } else {
                        System.out.println("Request " + request + ": Found " + hits.size() + " results.");
                    }

                    // Increment the request count
                    requestCount++;
                }

                // Track the real HTTP request count from Elasticsearch
                getRealTimeRequestCount(client);

                // Add a small delay between batches to simulate real-world usage
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("Error during request: " + e.getMessage());
            }
        }

        System.out.println("Simulation stopped.");
    }

    public static String getRandomAmenity(Random random) {
        String[] amenities = {"Free WiFi", "Pool", "Spa", "Parking", "Breakfast"};
        return amenities[random.nextInt(amenities.length)];
    }

    // Fetch real-time HTTP request count from Elasticsearch using _nodes/stats
    public static void getRealTimeRequestCount(ElasticsearchClient client) {
        try {
            // Request node stats to get HTTP request metrics
            NodesStatsRequest statsRequest = new NodesStatsRequest.Builder().build();
            NodesStatsResponse statsResponse = client.nodes().stats(statsRequest);

            // Get HTTP request stats (requests served)
            long requests = statsResponse.nodes().values().stream()
                    .mapToLong(nodeStats -> nodeStats.http().totalOpened())
                    .sum();

            // Output the real-time request count
            System.out.println("Real-time HTTP requests served by Elasticsearch: " + requests);
        } catch (Exception e) {
            System.err.println("Error fetching node stats: " + e.getMessage());
        }
    }

    // Method to get the current request count<
    public static int getRequestCount() {
        return requestCount;
    }
}