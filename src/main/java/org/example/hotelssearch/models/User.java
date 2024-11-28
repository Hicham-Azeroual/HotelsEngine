package org.example.hotelssearch.models;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class User {
    private String username;  // Unique identifier
    private String email;     // User's email
    private String password;  // Hashed password
    private String createdAt; // Timestamp of account creation as a string
    private String role;      // User role, default is "user"

    // Constructor
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now()); // Set the creation time to the current time as a string
        this.role = "user"; // Default role is "user"
    }

    public User() {

    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // toString() method for easy debugging
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}