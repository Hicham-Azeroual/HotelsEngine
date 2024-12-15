package org.example.hotelssearch.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class HotelCardController {

    public AnchorPane cardContainer;
    public VBox hotelImage;
    public VBox hotelInfo;
    public VBox hotelPrice;
    public VBox pricingContent;
    public Text guarantee;
    public Label roomPrice;
    public HBox reviews;
    public VBox heading;
    @FXML
    private ImageView hotelImageView;
    @FXML
    private Label hotelTitle;
    @FXML
    private Label hotelSubtitle;
    @FXML
    private Text hotelDetails;
    @FXML
    private Button viewRoomsButton;

    public void initialize() {
        // Example of setting dynamic content
        hotelTitle.setText("W Doha Hotel and Residences");
        hotelSubtitle.setText("Midtown East Grand Central");
        hotelDetails.setText("Ezdan Hotel & Suites offers a variety of suites and apartments for long and short stays in Doha.");

        // Set image dynamically
        Image image = new Image("http://www.brucall.com/wp-content/uploads/2017/06/hotel-tropical-island-hotel-matemo-island-resort-mozambique-comfortable.jpg");
        hotelImageView.setImage(image);

        // Add event for the button
        viewRoomsButton.setOnAction(event -> {
            System.out.println("View Rooms clicked");
        });
    }
}
