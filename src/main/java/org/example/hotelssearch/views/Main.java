package org.example.hotelssearch.views;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.example.hotelssearch.controllers.HotelController;
import org.example.hotelssearch.controllers.UserInterface;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/userInterface.fxml"));
            ScrollPane root = loader.load();

            // Get the controller instance
            UserInterface controller = loader.getController();

            // Set up the scene
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/styles/userInterface.css").toExternalForm());
            primaryStage.setTitle("Hotel Search");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}