package org.example.hotelssearch.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.hotelssearch.models.User;
import org.example.hotelssearch.utils.ElasticsearchConnection;

import java.io.IOException;
import java.util.*;

public class UserService {

    private static final String INDEX_NAME = "users";
    private final ElasticsearchClient client;
    private final ObservableList<Map.Entry<String, Integer>> actionStatistics = FXCollections.observableArrayList();

    public UserService() {
        try {
            ElasticsearchConnection.initializeClient();
            this.client = ElasticsearchConnection.getClient();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Elasticsearch client", e);
        }
    }

    public boolean signUp(User user) throws Exception {
        if (getUserByUsername(user.getUsername()) != null) {
            return false;
        }
        createUser(user);
        return true;
    }

    public User signIn(String username, String password) throws Exception {
        User user = getUserByUsername(username);
        if(user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public void createUser(User user) throws Exception {
        IndexRequest<User> request = new IndexRequest.Builder<User>()
                .index(INDEX_NAME)
                .id(user.getUsername())
                .document(user)
                .build();

        try {
            IndexResponse response = client.index(request);
            System.out.println("User created with ID: " + response.id());
        } catch (ElasticsearchException | IOException e) {
            System.err.println("Error creating user: " + e.getMessage());
            throw e;
        }
    }

    public User getUserByUsername(String username) throws Exception {
        GetRequest request = new GetRequest.Builder()
                .index(INDEX_NAME)
                .id(username)
                .build();

        try {
            GetResponse<User> response = client.get(request, User.class);
            if (response.found()) {
                return response.source();
            }
            System.out.println("User not found with username: " + username);
            return null;
        } catch (ElasticsearchException | IOException e) {
            System.err.println("Error getting user: " + e.getMessage());
            throw e;
        }
    }


    public ObservableList<Map.Entry<String, Integer>> getActionStatistics() {
        return actionStatistics;
    }
}
