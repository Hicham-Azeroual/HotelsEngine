package org.example.hotelssearch.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.hotelssearch.services.HotelService;
import org.example.hotelssearch.services.UserService;

import java.io.IOException;

public class Signin {

    Dashborad dashboard=new Dashborad();
    HotelService hotelService = new HotelService();
    public Button exitButton;
    public Button registerButton;
    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void handleSignIn() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        UserService userService = new UserService();

        // Perform sign-in validation (replace with actual logic)
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
        } else {
            try {
                // Successful sign-in logic (replace with actual logic)
                if (userService.signIn(username, password)!=null) {
                    navigateToHomePage();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed", "Username or password incorrect.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred during sign-in.");
                e.printStackTrace();
            }
        }
    }

    private void navigateToHomePage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomeView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 700, 500);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Home Page");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void exitApplication() {
        Platform.exit();
    }

}