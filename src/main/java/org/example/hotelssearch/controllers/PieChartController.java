package org.example.hotelssearch.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.example.hotelssearch.services.HotelService;

import java.util.Map;

public class PieChartController {

    @FXML
    private PieChart ratingPieChart;

    private HotelService hotelService; // Declare the hotel service

    // Constructor to inject the service
    public PieChartController() {
        this.hotelService = new HotelService();  // Or inject via dependency injection if needed
    }

    public void initialize() {
        // Fetch the data from the hotel service
        Map<String, Double> ratingData = hotelService.getRatingDistribution();

        // Create PieChart.Data objects and add them to the PieChart
        updatePieChart(ratingData);

        // Adding animation
        animatePieChart();
    }

    private void updatePieChart(Map<String, Double> ratingData) {
        // Clear previous data (if any)
        ratingPieChart.getData().clear();

        // Create PieChart.Data objects and use the percentage values directly from ratingData
        PieChart.Data rating1_2 = new PieChart.Data("Hotels with Rating 1-2", ratingData.get("1-2"));
        PieChart.Data rating2_3 = new PieChart.Data("Hotels with Rating 2-3", ratingData.get("2-3"));
        PieChart.Data rating3_4 = new PieChart.Data("Hotels with Rating 3-4", ratingData.get("3-4"));
        PieChart.Data rating4_5 = new PieChart.Data("Hotels with Rating 4-5", ratingData.get("4-5"));

        // Set the label for each slice using the percentage values
        updateSliceLabel(rating1_2, ratingData.get("1-2"));
        updateSliceLabel(rating2_3, ratingData.get("2-3"));
        updateSliceLabel(rating3_4, ratingData.get("3-4"));
        updateSliceLabel(rating4_5, ratingData.get("4-5"));

        // Add data to PieChart
        ratingPieChart.getData().addAll(rating1_2, rating2_3, rating3_4, rating4_5);

        // Add Tooltips for each slice
        addTooltips();
    }

    private void updateSliceLabel(PieChart.Data slice, double percentage) {
        // Format the label with the percentage value (as it's already a percentage)
        String label = String.format("%s - %.2f%%", slice.getName(), percentage);

        // Update slice label to include percentage
        slice.setName(label);
    }

    private void addTooltips() {
        // Tooltip message for each slice
        String tooltipMessage = "You can include stars to use this rating";

        // Add tooltip for each slice
        ratingPieChart.getData().forEach(data -> {
            Tooltip tooltip = new Tooltip(tooltipMessage);
            Tooltip.install(data.getNode(), tooltip);

            // Add custom hover style for each slice (optional)
            data.getNode().setOnMouseEntered(event -> {
                data.getNode().setStyle("-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 10, 0, 2, 2);");
            });
            data.getNode().setOnMouseExited(event -> {
                data.getNode().setStyle("");  // Reset the style after hover
            });
        });
    }

    private void animatePieChart() {
        // Use Timeline for smooth animation
        Timeline timeline = new Timeline();

        // Add a KeyFrame for each slice
        KeyFrame keyFrame1 = new KeyFrame(Duration.seconds(0), event -> {
            // Animate the fill of the slice
            ratingPieChart.getData().forEach(data -> {
                if (data.getName().contains("1-2")) {
                    data.getNode().setStyle("-fx-fill: transparent; -fx-pie-color: #FF5733;");
                }
            });
        });

        // You can adjust timings and styles here for a more engaging effect
        KeyFrame keyFrame2 = new KeyFrame(Duration.seconds(0.3), event -> {
            ratingPieChart.getData().forEach(data -> {
                if (data.getName().contains("2-3")) {
                    data.getNode().setStyle("-fx-fill: transparent; -fx-pie-color: #33FF57;");
                }
            });
        });

        KeyFrame keyFrame3 = new KeyFrame(Duration.seconds(0.6), event -> {
            ratingPieChart.getData().forEach(data -> {
                if (data.getName().contains("3-4")) {
                    data.getNode().setStyle("-fx-fill: transparent; -fx-pie-color: #3357FF;");
                }
            });
        });

        KeyFrame keyFrame4 = new KeyFrame(Duration.seconds(0.9), event -> {
            ratingPieChart.getData().forEach(data -> {
                if (data.getName().contains("4-5")) {
                    data.getNode().setStyle("-fx-fill: transparent; -fx-pie-color: #FF33A1;");
                }
            });
        });

        timeline.getKeyFrames().addAll(keyFrame1, keyFrame2, keyFrame3, keyFrame4);

        // Set the cycle count and start the animation
        timeline.setCycleCount(1);
        timeline.play();
    }
}
