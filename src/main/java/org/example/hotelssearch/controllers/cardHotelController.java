package org.example.hotelssearch.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import javafx.scene.layout.VBox;
import org.example.hotelssearch.services.HotelService;

import java.net.URL;
import java.util.ResourceBundle;

public class cardHotelController implements Initializable {

    @FXML
    private VBox container;

    @FXML
    private Button deleteButton;

    HotelService hotelService = new HotelService();

    private Dashborad dashboard;

    public void setDashboard(Dashborad dashboard) {
        this.dashboard = dashboard;
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
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {



    }
}
