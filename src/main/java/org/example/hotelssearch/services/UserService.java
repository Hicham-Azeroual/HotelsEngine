package org.example.hotelssearch.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.example.hotelssearch.models.User;
import org.example.hotelssearch.utils.ElasticsearchConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private static final String INDEX_NAME = "users";

    // Method to sign up a new user
    public void signUp(User user) throws Exception {
        // Check if the user already exists
        if (getUserByUsername(user.getUsername()) != null) {
            throw new Exception("User already exists");
        }

        // Create the new user
        createUser(user);
    }

    // Method to sign in a user
    public boolean signIn(String username, String password) throws Exception {
        User user = getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return true; // Authentication successful
        }
        return false; // Authentication failed
    }

    // Method to create a new user
    public void createUser(User user) throws Exception {
        ElasticsearchClient client = ElasticsearchConnection.getClient();
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

    // Method to get a user by username
    public User getUserByUsername(String username) throws Exception {
        ElasticsearchClient client = ElasticsearchConnection.getClient();
        GetRequest request = new GetRequest.Builder()
                .index(INDEX_NAME)
                .id(username)
                .build();

        try {
            GetResponse<User> response = client.get(request, User.class);
            if (response.found()) {
                return response.source();
            } else {
                System.out.println("User not found with username: " + username);
                return null;
            }
        } catch (ElasticsearchException | IOException e) {
            System.err.println("Error getting user: " + e.getMessage());
            throw e;
        }
    }

    // Method to update a user
    public void updateUser(User user) throws Exception {
        ElasticsearchClient client = ElasticsearchConnection.getClient();
        UpdateRequest<User, User> request = new UpdateRequest.Builder<User, User>()
                .index(INDEX_NAME)
                .id(user.getUsername())
                .doc(user)
                .build();

        try {
            UpdateResponse<User> response = client.update(request, User.class);
            System.out.println("User updated with ID: " + response.id());
        } catch (ElasticsearchException | IOException e) {
            System.err.println("Error updating user: " + e.getMessage());
            throw e;
        }
    }

    // Method to delete a user by username
    public void deleteUserByUsername(String username) throws Exception {
        ElasticsearchClient client = ElasticsearchConnection.getClient();
        DeleteRequest request = new DeleteRequest.Builder()
                .index(INDEX_NAME)
                .id(username)
                .build();

        try {
            DeleteResponse response = client.delete(request);
            System.out.println("User deleted with ID: " + response.id());
        } catch (ElasticsearchException | IOException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            throw e;
        }
    }

    // Method to search users by email
    public List<User> searchUsersByEmail(String email) throws Exception {
        ElasticsearchClient client = ElasticsearchConnection.getClient();
        SearchRequest request = new SearchRequest.Builder()
                .index(INDEX_NAME)
                .query(q -> q
                        .match(m -> m
                                .field("email")
                                .query(email)
                        )
                )
                .build();

        try {
            SearchResponse<User> response = client.search(request, User.class);
            List<Hit<User>> hits = response.hits().hits();
            List<User> users = new ArrayList<>();
            for (Hit<User> hit : hits) {
                users.add(hit.source());
            }
            return users;
        } catch (ElasticsearchException | IOException e) {
            System.err.println("Error searching users: " + e.getMessage());
            throw e;
        }
    }
}