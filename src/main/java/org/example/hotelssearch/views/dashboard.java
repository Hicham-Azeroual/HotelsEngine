package org.example.hotelssearch.views;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
public class dashboard extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(dashboard.class.getResource("/fxml/dashborad.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("LuxeStay");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
