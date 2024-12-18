package org.example.hotelssearch.controllers;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import javafx.scene.layout.VBox;
import org.example.hotelssearch.models.Hotel;
import org.example.hotelssearch.services.HotelService;

import java.net.URL;
import java.util.ResourceBundle;

public class cardHotelController implements Initializable {

    @FXML
    private VBox container;


    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    HotelService hotelService = new HotelService();

    private Dashborad dashboard;

    public void setDashboard(Dashborad dashboard) {
        this.dashboard = dashboard;
    }

    @FXML
    private void showUpdateHotel(ActionEvent event) {
        // Récupérer l'ID de l'hôtel à partir du bouton
        String hotelId = (String) ((Button) event.getSource()).getUserData();


        if (hotelId == null || hotelId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Hotel ID is missing.");
            return;
        }

        // Récupérer les données de l'hôtel à partir de l'ID
        Task<Hotel> task = new Task<>() {
            @Override
            protected Hotel call() throws Exception {
                return hotelService.retrieveHotelById(hotelId);
            }
        };

        task.setOnSucceeded(e -> {
            Hotel hotel = task.getValue();
            if (hotel == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Hotel not found.");
                return;
            }

            // Appeler la méthode du Dashboard pour afficher le formulaire
            if (dashboard != null) {
                dashboard.showUpdateForm(hotel);
            }
        });

        task.setOnFailed(e -> {
            Throwable error = task.getException();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve hotel: " + error.getMessage());
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    public void DeleteHotel(ActionEvent event) {
        // Récupérer le bouton qui a déclenché l'événement
        deleteButton = (Button) event.getSource();

        // Récupérer l'ID de l'hôtel à partir des données utilisateur du bouton
        String hotelId = (String) deleteButton.getUserData();
        System.out.println('A' + hotelId);

        // Valider l'ID de l'hôtel
        if (hotelId == null || hotelId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "ID de l'hôtel invalide.");
            return;
        }

        // Supprimer l'hôtel via le service
        boolean isDeleted = hotelService.deleteHotelById(hotelId);

        if (isDeleted) {
            // Si la suppression réussit
            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'hôtel a été supprimé avec succès.");

            // Recharger les hôtels après la suppression
            dashboard.reloadHotels();
        } else {
            // Si la suppression échoue
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression de l'hôtel.");
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setHotelId(String hotelId) {
        updateButton.setUserData(hotelId); // Stocker l'ID de l'hôtel dans le bouton
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {



    }
}
