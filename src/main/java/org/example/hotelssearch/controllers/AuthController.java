package org.example.hotelssearch.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.example.hotelssearch.models.User;
import org.example.hotelssearch.services.HotelService;
import org.example.hotelssearch.services.UserService;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

public class AuthController {

    Dashborad dashboard=new Dashborad();
    HotelService hotelService = new HotelService();
    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private TextField passwordshow;

    @FXML
    private CheckBox show;

    @FXML
    private Button login;

    @FXML
    private Hyperlink register;

    @FXML
    private AnchorPane login_form;

    @FXML
    private TextField username1;

    @FXML
    private PasswordField password1;

    @FXML
    private TextField passwordshow1;

    @FXML
    private CheckBox show1;

    @FXML
    private Button signup;

    @FXML
    private Hyperlink loginhere;

    @FXML
    private AnchorPane register_form;

    @FXML
    private TextField email;

    @FXML
    private Label focusDummy;

    @FXML
    private Label focusDummy1;

    private UserService userService;

    @FXML
    public void initialize() {
        userService = new UserService();
        focusDummy.requestFocus();
        focusDummy1.requestFocus();
    }

    public void loginShowPassword() {
        if (show.isSelected()) {
            passwordshow.setText(password.getText());
            passwordshow.setVisible(true);
            password.setVisible(false);
        } else {
            passwordshow.setVisible(false);
            password.setVisible(true);
        }
    }

    public void registerShowPassword() {
        if (show1.isSelected()) {
            passwordshow1.setText(password1.getText());
            passwordshow1.setVisible(true);
            password1.setVisible(false);
        } else {
            passwordshow1.setVisible(false);
            password1.setVisible(true);
        }
    }

    public void switchForm(ActionEvent event) {
        if (event.getSource() == loginhere) {
            register_form.setVisible(false);
            login_form.setVisible(true);
        } else if (event.getSource() == register) {
            login_form.setVisible(false);
            register_form.setVisible(true);
        }
    }
    @FXML
    public void handleSignUp() {
        String username1 = this.username1.getText();
        String password1 = show1.isSelected() ? this.passwordshow1.getText() : this.password1.getText();
        String email1 = this.email.getText();


        User user = new User(username1, email1, password1,"user" );
        System.out.println("Password during sign-up: " + password1); // Debugging line

        if (username1.isEmpty() || password1.isEmpty() || email1.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
        } else {
            try {
                if (userService.signUp(user)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Registration successful.");
                    // Navigate to the login page or perform other actions
                    register_form.setVisible(false);
                    login_form.setVisible(true);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed", "Registration failed. User already exists");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred during registration.");
                e.printStackTrace();
            }
        }
    }



    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }





//    private void switchToDashboard() {
//        try {
//            // Charger le fichier FXML
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
//            Parent dashboardRoot = loader.load();
//
//            // Récupérer le contrôleur associé
//            Dashborad dashboardController = loader.getController();
//
//            // Charger les hôtels et les afficher
//            dashboardController.displayHotels(HotelService.retrieveAllHotels());
//
//            // Configurer la nouvelle scène
//            Scene dashboardScene = new Scene(dashboardRoot);
//
//            // Obtenir la scène actuelle et la remplacer
//            Stage stage = (Stage) login.getScene().getWindow();
//            stage.setScene(dashboardScene);
//            stage.show();
//        } catch (IOException e) {
//            showAlert(Alert.AlertType.ERROR, "Failed to load the dashboard:", e.getMessage());
//        }
//    }


    public void handleSignIn(ActionEvent event) {
        String username = this.username.getText();
        String password = this.password.getText();
        try {
            boolean isAuthenticated = userService.signIn(username, password);
//            if (isAuthenticated) {
//                switchToDashboard();
//            } else {
//                showAlert(Alert.AlertType.valueOf("Authentication Failed"), "Invalid username or password.","username or password incorrect");
//            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "An error occurred during sign-in:", e.getMessage());
        }
    }
}