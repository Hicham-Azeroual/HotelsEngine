package org.example.hotelssearch.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.example.hotelssearch.models.Hotel;
import org.example.hotelssearch.services.HotelService;
import org.example.hotelssearch.utils.GoogleMapsGeocoding;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Dashborad {
    private static final int PAGE_SIZE = 10; // Number of hotels per page
    private int currentPage = 0; // Current page index

    @FXML
    private TextField nameField;

    @FXML
    private TextField linkField;

    @FXML
    private TextField latitudeField;

    @FXML
    private TextField longitudeField;

    @FXML
    private TextField checkInTimeField;

    @FXML
    private TextField checkOutTimeField;

    @FXML
    private TextField lowestRateField;

    @FXML
    private TextField overallRatingField;

    @FXML
    private TextField reviewsField;

    @FXML
    private TextArea amenitiesField;

    @FXML
    public ScrollPane scrollpane1;

    @FXML
    public ScrollPane scrollpane2;

    @FXML
    public Label numOfHotel;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button manageButton;

    @FXML
    private Button signOutButton;

    @FXML
    private VBox container;

    @FXML
    private StackPane contentStack;

    @FXML
    private AnchorPane dashboardPane;

    @FXML
    private AnchorPane managePane;

    @FXML
    private AnchorPane statisticsPane;

    @FXML
    private Button createHotel;

    @FXML
    private AnchorPane AddHotel;

    @FXML
    private Button loadMoreButton;

    private final HotelService hotelService = new HotelService();

    @FXML
    private void showDashboard() {
        updateActiveButton(dashboardButton);
        dashboardButton.getStyleClass().add("card");
        scrollpane2.setVisible(true);
        scrollpane1.setVisible(false);
        AddHotel.setVisible(false);
    }

    @FXML
    private void showCreateHotel(ActionEvent event) {
        scrollpane2.setVisible(false);
        scrollpane1.setVisible(false);
        AddHotel.setVisible(true);
    }

    @FXML
    private void showManage() {
        updateActiveButton(manageButton);
        scrollpane2.setVisible(false);
        scrollpane1.setVisible(true);
        AddHotel.setVisible(false);

        // Show loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        container.getChildren().add(loadingIndicator);

        // Execute the retrieval task in a separate thread
        Task<List<Hotel>> task = new Task<>() {
            @Override
            protected List<Hotel> call() throws Exception {
                return hotelService.retrieveAllHotels(1, 15);
            }
        };

        task.setOnSucceeded(event -> {
            List<Hotel> hotels = task.getValue();
            container.getChildren().remove(loadingIndicator); // Remove loading indicator
            displayHotels(hotels); // Display hotels
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            container.getChildren().remove(loadingIndicator); // Remove loading indicator
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve hotels: " + error.getMessage());
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    @FXML
    private void signOut() {
        updateActiveButton(signOutButton);
    }

    private void updateActiveButton(Button activeButton) {
        if (dashboardButton != null) {
            dashboardButton.getStyleClass().remove("cardActif");
        }
        if (manageButton != null) {
            manageButton.getStyleClass().remove("cardActif");
        }
        if (signOutButton != null) {
            signOutButton.getStyleClass().remove("cardActif");
        }

        if (activeButton != null && !activeButton.getStyleClass().contains("cardActif")) {
            activeButton.getStyleClass().add("cardActif");
        }
    }


    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void displayHotels(List<Hotel> hotels) {
        container.getChildren().clear();

        int startIndex = currentPage * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, hotels.size());

        for (int i = startIndex; i < endIndex; i++) {
            Hotel hotel = hotels.get(i);
            executorService.submit(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HotelCard.fxml"));
                    AnchorPane card = loader.load();

                    cardHotelController cardController = loader.getController();
                    cardController.setDashboard(this);

                    fillHotelCard(card, hotel);
                    Platform.runLater(() -> container.getChildren().add(card));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Add a "Load More" button if there are more hotels to load
        if (endIndex < hotels.size()) {
            Button loadMoreButton = new Button("Load More");
            loadMoreButton.setOnAction(event -> {
                currentPage++;
                displayHotels(hotels);
            });
            container.getChildren().add(loadMoreButton);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        System.out.println("Initializing Dashboard");
        if (dashboardButton == null) {
            System.out.println("dashboardButton is null");
        } else {
            System.out.println("dashboardButton is not null");
        }
        showDashboard();
    }

    public void reloadHotels() {
        container.getChildren().clear();
        currentPage = 0; // Reset the current page

        // Show loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        container.getChildren().add(loadingIndicator);

        Task<List<Hotel>> task = new Task<>() {
            @Override
            protected List<Hotel> call() throws Exception {
                return hotelService.retrieveAllHotels(1, 15);
            }
        };

        task.setOnSucceeded(event -> {
            List<Hotel> hotels = task.getValue();
            container.getChildren().remove(loadingIndicator); // Remove loading indicator
            displayHotels(hotels); // Display hotels
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            container.getChildren().remove(loadingIndicator); // Remove loading indicator
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to retrieve hotels: " + error.getMessage());
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    public void fillHotelCard(AnchorPane card, Hotel hotel) throws Exception {
        // Recherche des éléments FXML
        ImageView imageView = (ImageView) card.lookup("#imageView"); // Utilisez l'ID correct
        Label nameLabel = (Label) card.lookup(".label-name");
        Label locationLabel = (Label) card.lookup(".label-location");
        Label ratingLabel = (Label) card.lookup(".label-rating");
        Label priceLabel = (Label) card.lookup(".label-price");
        FlowPane amenitiesFlowPane = (FlowPane) card.lookup("#amenitiesFlowPane"); // Recherche de la HBox pour les amenities

        // Récupérer le bouton deleteButton
        Button deleteButton = (Button) card.lookup("#deleteButton");

        // Remplir les informations de base
        if (nameLabel != null) nameLabel.setText(hotel.getName());
        if (locationLabel != null) {
            locationLabel.setText(GoogleMapsGeocoding.getDetailedLocation(hotel.getGps_coordinates()));
        }
        if (ratingLabel != null) {
            double overallRating = hotel.getOverall_rating();
            ratingLabel.setText(String.format("%.1f", overallRating)); // Affiche un seul chiffre après la virgule
        }
        if (priceLabel != null) priceLabel.setText(hotel.getLowest_rate() + " MAD");

        if (imageView != null) {
            Image image = new Image(hotel.getImages()[0]);
            imageView.setImage(image);
        }

        // Remplir les amenities
        if (amenitiesFlowPane != null) {
            // Nettoyer le FlowPane avant d'ajouter de nouveaux éléments
            amenitiesFlowPane.getChildren().clear();

            // Récupérer le tableau d'amenities
            String[] amenities = hotel.getAmenities();

            // Parcourir le tableau d'amenities
            for (int i = 0; i < amenities.length; i++) {
                String amenity = amenities[i];
                Label amenityLabel = new Label(amenity);
                amenityLabel.setFont(Font.font("System", 12)); // Réduire la taille de la police
                amenitiesFlowPane.getChildren().add(amenityLabel);

                // Ajouter une virgule après chaque amenity, sauf le dernier
                if (i < amenities.length - 1) {
                    Label commaLabel = new Label(",");
                    commaLabel.setFont(Font.font("System", 12)); // Réduire la taille de la police
                    amenitiesFlowPane.getChildren().add(commaLabel);
                }
            }
        }


        // Vérifier si deleteButton est null
        if (deleteButton != null) {
            deleteButton.setUserData(hotel.get_id());
        }
    }

    @FXML
    private void addHotel(ActionEvent event) {
        try {
            // Récupérer les données du formulaire
            String name = nameField.getText();
            String link = linkField.getText();
            float latitude = Float.parseFloat(latitudeField.getText());
            float longitude = Float.parseFloat(longitudeField.getText());
            String checkInTime = checkInTimeField.getText();
            String checkOutTime = checkOutTimeField.getText();
            float lowestRate = Float.parseFloat(lowestRateField.getText()); // Utilisation de Float.parseFloat
            float overallRating = Float.parseFloat(overallRatingField.getText()); // Utilisation de Float.parseFloat
            int reviews = Integer.parseInt(reviewsField.getText());
            String[] amenities = amenitiesField.getText().split(",\\s*"); // Séparer les équipements par des virgules

            Hotel hotel = new Hotel(
                    name,
                    link,
                    new Hotel.GpsCoordinates(latitude, longitude),
                    checkInTime,
                    checkOutTime,
                    lowestRate,
                    new String[]{"default.jpg"}, // Ajouter une image par défaut si nécessaire
                    overallRating,
                    reviews,
                    amenities,
                    null // ID sera généré par la base de données
            );

            boolean isCreated = hotelService.createHotel(hotel);

            if (isCreated) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "The hotel has been added successfully.");
                clearForm(); // Réinitialiser le formulaire
                reloadHotels(); // Recharger la liste des hôtels
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add the hotel.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter valid numeric values.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    private void clearForm() {
        nameField.clear();
        linkField.clear();
        latitudeField.clear();
        longitudeField.clear();
        checkInTimeField.clear();
        checkOutTimeField.clear();
        lowestRateField.clear();
        overallRatingField.clear();
        reviewsField.clear();
        amenitiesField.clear();
    }
    @FXML
    private void loadMoreHotels() {
        currentPage++; // Incrémenter la page actuelle
        displayHotels(hotelService.retrieveAllHotels(1, 15)); // Recharger les hôtels
    }
}