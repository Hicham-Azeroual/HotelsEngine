package org.example.hotelssearch.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import org.example.hotelssearch.services.HotelService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserRequestReelTime {

    @FXML
    private LineChart<Number, Number> userActivityChart;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    private HotelService hotelService;
    private ScheduledExecutorService scheduler;
    private XYChart.Series<Number, Number> series;
    private int timeCounter = 0;

    @FXML
    private void initialize() {
        hotelService = new HotelService();
        series = new XYChart.Series<>();
        series.setName("User Requests");
        userActivityChart.getData().add(series);

        // Apply custom styles
        userActivityChart.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10px;");
        xAxis.setStyle("-fx-tick-label-fill: #333333; -fx-font-size: 14px;");
        yAxis.setStyle("-fx-tick-label-fill: #333333; -fx-font-size: 14px;");

        // Start the real-time update task
        startRealTimeUpdates();
    }

    private void startRealTimeUpdates() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            // Fetch the random number of requests
            int numberOfRequests = generateRandomNumberOfRequests();

            // Update the chart on the JavaFX Application Thread
            Platform.runLater(() -> {
                series.getData().add(new XYChart.Data<>(timeCounter, numberOfRequests));

                // Remove old data if necessary
                if (series.getData().size() > 60) {
                    series.getData().remove(0);
                }

                // Update the x-axis range
                xAxis.setLowerBound(timeCounter - 60);
                xAxis.setUpperBound(timeCounter);

                timeCounter++;
            });
        }, 0, 1, TimeUnit.SECONDS);
    }

    private int generateRandomNumberOfRequests() {
        // Generate a random number of requests between 0 and 100
        return (int) (Math.random() * 100);
    }

    public void stopRealTimeUpdates() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}