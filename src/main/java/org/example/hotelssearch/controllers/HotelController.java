package org.example.hotelssearch.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.hotelssearch.models.Hotel;
import org.example.hotelssearch.services.HotelService;

import java.io.IOException;
import java.util.List;

public class HotelController {



    @FXML
    private TextField email;

    @FXML
    private Button login;

    @FXML
    private Hyperlink loginhere;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField password1;

    @FXML
    private TextField passwordshow;

    @FXML
    private TextField passwordshow1;

    @FXML
    private Hyperlink register;

    @FXML
    private CheckBox show;

    @FXML
    private CheckBox show1;

    @FXML
    private Button signup;

    @FXML
    private TextField username;

    @FXML
    private TextField username1;


    @FXML
    private Label focusDummy;

    @FXML
    private Label focusDummy1;
    @FXML
    private AnchorPane register_form;

    @FXML
    private AnchorPane login_form;

    @FXML
    public void initialize() {
        focusDummy.requestFocus();
        focusDummy1.requestFocus();
    }

    public void loginShowPassword() {

        if (show.isSelected()) {
            passwordshow.setText(password.getText());
            passwordshow.setVisible(true);
            password.setVisible(false);
        }else {
            passwordshow.setVisible(false);
            password.setVisible(true);
        }
    }
    public void registerShowPassword() {

        if (show1.isSelected()) {
            passwordshow1.setText(password1.getText());
            passwordshow1.setVisible(true);
            password1.setVisible(false);
        }else {
            passwordshow1.setVisible(false);
            password1.setVisible(true);
        }
    }
    public void switchForm(ActionEvent event) {
        if(event.getSource() == loginhere) {
            register_form.setVisible(false);
            login_form.setVisible(true);
        } else if (event.getSource() == register) {
            login_form.setVisible(false);
            register_form.setVisible(true);
            
        }
    }

}
