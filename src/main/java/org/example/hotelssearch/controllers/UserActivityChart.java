package org.example.hotelssearch.controllers;

import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import org.example.hotelssearch.services.UserService;

import java.util.Map;

public class UserActivityChart {
    public static void showChart(UserService userService) {
        Stage chartStage = new Stage();

        // Define axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("User");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Actions Performed");

        // Create bar chart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("User Actions Statistics");

        // Populate data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Integer> entry : userService.getActionStatistics()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        if (series.getData().isEmpty()) {
            System.out.println("No data available for chart.");
        }

        barChart.getData().add(series);

        // Show chart
        Scene scene = new Scene(barChart, 800, 600);
        chartStage.setTitle("User Action Chart");
        chartStage.setScene(scene);
        chartStage.show();
    }
}
