package org.example.hotelssearch.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.util.Duration;
import org.example.hotelssearch.controllers.HotelRequestSimulator;

public class RequestChartController {

    @FXML
    private LineChart<Number, Number> requestChart;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private Button startButton;

    @FXML
    private Button stopButton;

    private XYChart.Series<Number, Number> series;
    private Timeline timeline;
    private int requestCount = 0;

    @FXML
    private void initialize() {
        // Initialize the series
        series = new XYChart.Series<>();
        series.setName("HTTP Requests");
        requestChart.getData().add(series);

        // Configure the timeline to update the chart every second
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateChart();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);

        // Disable the stop button initially
        stopButton.setDisable(true);
    }

    @FXML
    private void startSimulation() {
        // Start the simulation and the timeline
        HotelRequestSimulator.startSimulation();
        timeline.play();

        // Disable the start button and enable the stop button
        startButton.setDisable(true);
        stopButton.setDisable(false);
    }

    @FXML
    private void stopSimulation() {
        // Stop the simulation and the timeline
        HotelRequestSimulator.stopSimulation();
        timeline.stop();

        // Enable the start button and disable the stop button
        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

    private void updateChart() {
        // Add a new data point to the series
        series.getData().add(new XYChart.Data<>(series.getData().size(), requestCount));

        // Remove old data points if the series is too long
        if (series.getData().size() > 60) {
            series.getData().remove(0);
        }

        // Update the request count (this should be updated from the simulator)
        requestCount = HotelRequestSimulator.getRequestCount();
    }
}