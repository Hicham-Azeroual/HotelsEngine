package org.example.hotelssearch.views;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import org.example.hotelssearch.controllers.Dashborad;
import org.example.hotelssearch.models.Hotel;
import org.example.hotelssearch.services.HotelService;

import java.io.IOException;
import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hello-view.fxml"));
            BorderPane root = loader.load();

            // Récupérer le contrôleur
            Dashborad controller = loader.getController();

            // Créer une tâche pour récupérer les hôtels de manière asynchrone
            Task<List<Hotel>> task = HotelService.retrieveAllHotels();

            // Configurer l'action lorsque la tâche est réussie
            task.setOnSucceeded(event -> {
                List<Hotel> hotels = task.getValue();
                controller.displayHotels(hotels); // Afficher les hôtels dans l'interface utilisateur
            });

            // Configurer l'action en cas d'échec
            task.setOnFailed(event -> {
                Throwable error = task.getException();
                System.err.println("Erreur lors de la récupération des hôtels : " + error.getMessage());
            });

            // Exécuter la tâche dans un thread séparé
            Thread thread = new Thread(task);
            thread.setDaemon(true); // Assure que le thread s'arrête avec l'application
            thread.start();

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
