package org.example.hotelssearch.views;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger le fichier FXML de la vue de login/register
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hello-view.fxml"));
            AnchorPane root = loader.load();
            // Créer la scène et afficher la fenêtre principale
            Scene scene = new Scene(root);
            primaryStage.setTitle("luxeStay");
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