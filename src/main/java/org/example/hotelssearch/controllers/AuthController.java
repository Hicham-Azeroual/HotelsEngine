package org.example.hotelssearch.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.example.hotelssearch.models.User;
import org.example.hotelssearch.services.UserService;
import org.mindrot.jbcrypt.BCrypt;

public class AuthController {

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
    public void handleSignIn() {
        String username = this.username.getText();
        String password = this.password.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
        } else {
            try {
                if (userService.signIn(username, password)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Login successful.");
                    // Navigate to the home page or perform other actions
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed", "Username or password incorrect.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred during sign-in.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleSignUp() {
        String username1 = this.username1.getText();
        String password1 = show1.isSelected() ? this.passwordshow1.getText() : this.password1.getText();
        String email1 = this.email.getText();

        // Hash the password before storing it
        String hashedPassword = BCrypt.hashpw(password1, BCrypt.gensalt());

        User user = new User(username1, email1, hashedPassword);
        System.out.println("Password during sign-up: " + password1); // Debugging line

        if (username1.isEmpty() || password1.isEmpty() || email1.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
        } else {
            try {
                if (userService.signUp(user)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Registration successful.");
                    // Navigate to the login page or perform other actions
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
}