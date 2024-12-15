package org.example.hotelssearch.views;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class chart extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(dashboard.class.getResource("/fxml/pieChart.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("LuxeStay");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
