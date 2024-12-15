package org.example.hotelssearch.utils;

import org.example.hotelssearch.models.User;
import java.util.*;
import java.util.concurrent.*;

public class UserActivitySimulation {
    public static void main(String[] args) {
        List<User> users = new ArrayList<>();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(5); // 5 threads for user actions

        // Create 5 users
        for (int i = 1; i <= 5; i++) {
            users.add(new User("User" + i, "user" + i + "@example.com", "password" + i));
        }

        // Simulate user actions
        for (User user : users) {
            executor.scheduleAtFixedRate(() -> {
                user.incrementActionsPerformed();
                System.out.println(user.getUsername() + " performed an action. Total: " + user.getActionsPerformed());
            }, 0, 2, TimeUnit.SECONDS); // Action every 2 seconds
        }

        // Schedule stopping the simulation after 20 seconds
        executor.schedule(() -> {
            executor.shutdown();
            System.out.println("Simulation ended.");
            users.forEach(System.out::println); // Print final user stats
        }, 20, TimeUnit.SECONDS);
    }
}