package org.example.hotelssearch.utils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.apache.http.Header;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import javax.net.ssl.SSLContext;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

import java.io.IOException;
import java.security.cert.X509Certificate;

public class ElasticsearchConnection {
    private static final String SERVER_URL = "https://localhost:9200";
    private static final String API_KEY = "UVJnYzNwSUJjWTNfdERqbElkY206RXBoSnU3WWhUcU8xS3NiWlI1NC1pQQ==";
    private static ElasticsearchClient esClient;

    // Initialize Elasticsearch Client
    public static synchronized void initializeClient() throws Exception {
        // Avoid reinitializing the client if already initialized
        if (esClient != null) {
            return; // Return if the client is already initialized
        }

        // Disable SSL certificate verification for local testing
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial((TrustStrategy) (X509Certificate[] chain, String authType) -> true)
                .build();

        // Create the low-level client
        RestClientBuilder builder = RestClient.builder(HttpHost.create(SERVER_URL))
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Authorization", "ApiKey " + API_KEY)
                })
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setSSLContext(sslContext);
                    }
                });

        RestClient restClient = builder.build();

        // Create the ObjectMapper and register the JavaTimeModule
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));

        // And create the API client
        esClient = new ElasticsearchClient(transport);
    }

    // Get Elasticsearch Client instance
    public static ElasticsearchClient getClient() {
        return esClient;
    }

    // Close Elasticsearch Client
    public static synchronized void closeClient() {
        if (esClient != null) {
            try {
                esClient._transport().close();
                esClient = null; // Ensure the client is set to null after closing
            } catch (Exception e) {
                System.err.println("Error closing Elasticsearch transport: " + e.getMessage());
            }
        }
    }

    // Check if the Elasticsearch connection is active
    public static boolean isConnectionActive() {
        try {
            if (esClient != null) {
                esClient.cluster().health(h -> h);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Connection failed due to IO exception: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
        return false;
    }

    public static void main(String[] args) {
        try {
            // Initialize the client
            initializeClient();

            // Check the connection
            if (isConnectionActive()) {
                System.out.println("Connection to Elasticsearch is successful!");
            } else {
                System.out.println("Connection to Elasticsearch failed.");
            }

            // Close the client
            closeClient();
        } catch (Exception e) {
            System.err.println("Error initializing or testing Elasticsearch connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}