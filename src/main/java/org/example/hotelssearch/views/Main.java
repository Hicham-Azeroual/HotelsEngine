package org.example.hotelssearch.views;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashborad.fxml"));
            BorderPane root = loader.load();

            // Create a scene
            Scene scene = new Scene(root, 1099, 724);

            // Load and apply the CSS stylesheet
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

            // Set the title and display the stage
            primaryStage.setTitle("Dashboard");
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
