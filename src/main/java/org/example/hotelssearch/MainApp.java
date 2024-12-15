package org.example.hotelssearch;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.hotelssearch.controllers.Dashborad;
import org.example.hotelssearch.models.Hotel;
import org.example.hotelssearch.models.User;
import org.example.hotelssearch.services.HotelService;
import org.example.hotelssearch.services.UserService;
import org.example.hotelssearch.views.dashboard;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainApp  extends Application {


    @Override
    public void start(Stage stage) throws IOException {

        HotelService hotelService = new HotelService();

        // Créer une tâche pour récupérer tous les hôtels

        Task<List<Hotel>> task = HotelService.retrieveAllHotels();
        System.out.println(1);
        // Ajouter un écouteur pour afficher les hôtels une fois récupérés
        task.setOnSucceeded(event -> {
            List<Hotel> hotels = task.getValue();
            System.out.println("Hotels retrieved successfully: " + hotels.size());
            for (Hotel hotel : hotels) {
                System.out.println(hotel); // Afficher chaque hôtel dans la console
            }
        });
        task.setOnRunning(event -> {
            System.out.println("Task is running...");
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            System.err.println("Failed to retrieve hotels: " + error.getMessage());
        });

        // Exécuter la tâche dans un thread
        Thread thread = new Thread(task);
        thread.setDaemon(true); // Assure que le thread s'arrête avec l'application
        thread.start();

    }
    public static void main(String[] args) throws Exception {
        // Initialiser le service
        launch(args);
    }}