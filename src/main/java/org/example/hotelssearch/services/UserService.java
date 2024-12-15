//package org.example.hotelssearch.services;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch._types.ElasticsearchException;
//import co.elastic.clients.elasticsearch.core.*;
//import co.elastic.clients.elasticsearch.core.search.Hit;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import org.example.hotelssearch.models.User;
//import org.example.hotelssearch.utils.ElasticsearchConnection;
//
//import java.io.IOException;
//import java.util.*;
//
//public class UserService {
//
//    private static final String INDEX_NAME = "users";
//    private final ElasticsearchClient client;
//    private final ObservableList<Map.Entry<String, Integer>> actionStatistics = FXCollections.observableArrayList();
//
//    public UserService() {
//        try {
//            ElasticsearchConnection.initializeClient();
//            this.client = ElasticsearchConnection.getClient();
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to initialize Elasticsearch client", e);
//        }
//    }
//
//    public boolean signUp(User user) throws Exception {
//        if (getUserByUsername(user.getUsername()) != null) {
//            return false;
//        }
//        createUser(user);
//        return true;
//    }
//
//    public boolean signIn(String username, String password) throws Exception {
//        User user = getUserByUsername(username);
//        return user != null && user.getPassword().equals(password);
//    }
//
//    public void createUser(User user) throws Exception {
//        IndexRequest<User> request = new IndexRequest.Builder<User>()
//                .index(INDEX_NAME)
//                .id(user.getUsername())
//                .document(user)
//                .build();
//
//        try {
//            IndexResponse response = client.index(request);
//            System.out.println("User created with ID: " + response.id());
//        } catch (ElasticsearchException | IOException e) {
//            System.err.println("Error creating user: " + e.getMessage());
//            throw e;
//        }
//    }
//
//    public User getUserByUsername(String username) throws Exception {
//        GetRequest request = new GetRequest.Builder()
//                .index(INDEX_NAME)
//                .id(username)
//                .build();
//
//        try {
//            GetResponse<User> response = client.get(request, User.class);
//            if (response.found()) {
//                return response.source();
//            }
//            System.out.println("User not found with username: " + username);
//            return null;
//        } catch (ElasticsearchException | IOException e) {
//            System.err.println("Error getting user: " + e.getMessage());
//            throw e;
//        }
//    }
//
//    public void simulateUserActions() {
//        Random random = new Random();
//        actionStatistics.clear();
//
//        try {
//            for (int i = 1; i <= 10; i++) {
//                String username = "User" + i;
//                String email = "user" + i + "@example.com";
//                String password = "password" + i;
//
//                User user = new User(username, email, password);
//                if (getUserByUsername(username) == null) {
//                    createUser(user);
//                }
//
//                int actions = random.nextInt(50) + 1;
//                for (int j = 0; j < actions; j++) {
//                    user.incrementActionsPerformed();
//                }
//
//                actionStatistics.add(Map.entry(username, user.getActionsPerformed()));
//            }
//        } catch (Exception e) {
//            System.err.println("Error during simulation: " + e.getMessage());
//        }
//    }
//
//    public ObservableList<Map.Entry<String, Integer>> getActionStatistics() {
//        return actionStatistics;
//    }
//}
