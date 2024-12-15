//package org.example.hotelssearch.controllers;
//
//import javafx.concurrent.Task;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.control.*;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.*;
//import javafx.scene.text.Font;
//import org.example.hotelssearch.models.Hotel;
//import org.example.hotelssearch.services.HotelService;
//import org.example.hotelssearch.utils.GPSCoordinates;
//import org.example.hotelssearch.utils.GeocodingService;
//
//
//import java.util.List;
//
//public class Dashborad {
//
//    @FXML
//    private TextField nameField;
//
//    @FXML
//    private TextField linkField;
//
//    @FXML
//    private TextField latitudeField;
//
//    @FXML
//    private TextField longitudeField;
//
//    @FXML
//    private TextField checkInTimeField;
//
//    @FXML
//    private TextField checkOutTimeField;
//
//    @FXML
//    private TextField lowestRateField;
//
//    @FXML
//    private TextField overallRatingField;
//
//    @FXML
//    private TextField reviewsField;
//
//    @FXML
//    private TextArea amenitiesField;
//
//    @FXML
//    public ScrollPane scrollpane1;
//
//    @FXML
//    public ScrollPane scrollpane2;
//
//    @FXML
//    public Label numOfHotel;
//
//    @FXML
//    private Button dashboardButton;
//
//    @FXML
//    private Button manageButton;
//
//    @FXML
//    private Button signOutButton;
//
//    @FXML
//    private VBox container;
//
//    @FXML
//    private StackPane contentStack;
//
//    @FXML
//    private AnchorPane dashboardPane;
//
//    @FXML
//    private AnchorPane managePane;
//
//    @FXML
//    private AnchorPane statisticsPane;
//
//    @FXML
//    private Button createHotel;
//
//    @FXML
//    private AnchorPane AddHotel;
//
//    private final HotelService hotelService = new HotelService();
//
//    @FXML
//    private void showDashboard() {
//        updateActiveButton(dashboardButton);
//        dashboardButton.getStyleClass().add("card");
//        scrollpane2.setVisible(true);
//        scrollpane1.setVisible(false);
//        AddHotel.setVisible(false);
//    }
//
//    @FXML
//    private void showCreateHotel(ActionEvent event) {
//        scrollpane2.setVisible(false);
//        scrollpane1.setVisible(false);
//        AddHotel.setVisible(true);
//    }
//
//
//    @FXML
//    private void showManage() {
//        updateActiveButton(manageButton);
//        scrollpane2.setVisible(false);
//        scrollpane1.setVisible(true);
//        AddHotel.setVisible(false);
//
//        // Exécute la récupération des hôtels dans un thread séparé
//        Task<List<Hotel>> task = HotelService.retrieveAllHotels();
//
//        // Ajouter un écouteur pour afficher les hôtels une fois récupérés
//        task.setOnSucceeded(event -> {
//            List<Hotel> hotels = task.getValue();
//            displayHotels(hotels);
//            System.out.println("ok");// Affiche les hôtels dans l'interface
//        });
//        task.setOnRunning(event -> {
//            System.out.println("Task is running...");
//        });
//        task.setOnFailed(event -> {
//            Throwable error = task.getException();
//            System.err.println("Failed to retrieve hotels: " + error.getMessage());
//        });
//
//        // Exécuter la tâche dans un thread
//        System.out.println("Starting hotel retrieval task...");
//        Thread thread = new Thread(task);
//        thread.setDaemon(true); // Assure que le thread s'arrête avec l'application
//        thread.start();
////        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
////            System.err.println("Uncaught exception in thread " + thread.getName() + ": " + throwable.getMessage());
////            throwable.printStackTrace();
////        });
//    }
//
//
//    @FXML
//    private void signOut() {
//        updateActiveButton(signOutButton);
//    }
//
//    private void updateActiveButton(Button activeButton) {
//        if (dashboardButton != null) {
//            dashboardButton.getStyleClass().remove("cardActif");
//        }
//        if (manageButton != null) {
//            manageButton.getStyleClass().remove("cardActif");
//        }
//        if (signOutButton != null) {
//            signOutButton.getStyleClass().remove("cardActif");
//        }
//
//        if (activeButton != null && !activeButton.getStyleClass().contains("cardActif")) {
//            activeButton.getStyleClass().add("cardActif");
//        }
//    }
//
//    public void displayHotels(List<Hotel> hotels) {
//        System.out.println("Displaying hotels in UI...");
//        container.getChildren().clear();
//
//        for (Hotel hotel : hotels) {
//            try {
//                System.out.println("Adding hotel to UI: " + hotel.getName());
//                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HotelCard.fxml"));
//                AnchorPane card = loader.load();
//
//                // Récupérer le contrôleur de la carte d'hôtel
//                cardHotelController cardController = loader.getController();
//
//                // Passer une référence à Dashborad
//                cardController.setDashboard(this);
//
//                fillHotelCard(card, hotel);
//                container.getChildren().add(card);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        System.out.println("Finished displaying hotels.");
//    }
//
//
//    private void showAlert(Alert.AlertType alertType, String title, String message) {
//        Alert alert = new Alert(alertType);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//
//    @FXML
//    public void initialize() {
//        System.out.println("Initializing Dashboard");
//        if (dashboardButton == null) {
//            System.out.println("dashboardButton is null");
//        } else {
//            System.out.println("dashboardButton is not null");
//        }
//        showDashboard();
//    }
//    public void reloadHotels() {
//
//        container.getChildren().clear();
//
//        // Exécute la récupération des hôtels dans un thread séparé
//        Task<List<Hotel>> task = HotelService.retrieveAllHotels();
//
//        // Ajouter un écouteur pour afficher les hôtels une fois récupérés
//        task.setOnSucceeded(event -> {
//            List<Hotel> hotels = task.getValue();
//            displayHotels(hotels); // Affiche les hôtels dans l'interface
//        });
//
//        task.setOnRunning(event -> {
//            System.out.println("Task is running...");
//        });
//
//        task.setOnFailed(event -> {
//            Throwable error = task.getException();
//            System.err.println("Failed to retrieve hotels: " + error.getMessage());
//        });
//
//        // Exécuter la tâche dans un thread
//        System.out.println("Starting hotel retrieval task...");
//        Thread thread = new Thread(task);
//        thread.setDaemon(true); // Assure que le thread s'arrête avec l'application
//        thread.start();
//    }
//
//
//    public void fillHotelCard(AnchorPane card, Hotel hotel) throws Exception {
//        // Recherche des éléments FXML
//        ImageView imageView = (ImageView) card.lookup("#imageView"); // Utilisez l'ID correct
//        Label nameLabel = (Label) card.lookup(".label-name");
//        Label locationLabel = (Label) card.lookup(".label-location");
//        Label ratingLabel = (Label) card.lookup(".label-rating");
//        Label priceLabel = (Label) card.lookup(".label-price");
//        FlowPane amenitiesFlowPane = (FlowPane) card.lookup("#amenitiesFlowPane"); // Recherche de la HBox pour les amenities
//
//        // Récupérer le bouton deleteButton
//        Button deleteButton = (Button) card.lookup("#deleteButton");
//
//        // Remplir les informations de base
//        if (nameLabel != null) nameLabel.setText(hotel.getName());
//        if (locationLabel != null) {
//            locationLabel.setText(GeocodingService.getDetailedLocation(new GPSCoordinates(hotel.getGps_coordinates().getLatitude(), hotel.getGps_coordinates().getLongitude())));
//        }
//        if (ratingLabel != null) {
//            double overallRating = hotel.getOverall_rating();
//            ratingLabel.setText(String.format("%.1f", overallRating)); // Affiche un seul chiffre après la virgule
//        }
//        if (priceLabel != null) priceLabel.setText(hotel.getLowest_rate() + " MAD");
//
//        if (imageView != null) {
//            Image image = new Image(hotel.getImages()[0]);
//            imageView.setImage(image);
//        }
//
//        // Remplir les amenities
//        if (amenitiesFlowPane != null) {
//            // Nettoyer le FlowPane avant d'ajouter de nouveaux éléments
//            amenitiesFlowPane.getChildren().clear();
//
//            // Récupérer le tableau d'amenities
//            String[] amenities = hotel.getAmenities();
//
//            // Parcourir le tableau d'amenities
//            for (int i = 0; i < amenities.length; i++) {
//                String amenity = amenities[i];
//                Label amenityLabel = new Label(amenity);
//                amenityLabel.setFont(Font.font("System", 12)); // Réduire la taille de la police
//                amenitiesFlowPane.getChildren().add(amenityLabel);
//
//                // Ajouter une virgule après chaque amenity, sauf le dernier
//                if (i < amenities.length - 1) {
//                    Label commaLabel = new Label(",");
//                    commaLabel.setFont(Font.font("System", 12)); // Réduire la taille de la police
//                    amenitiesFlowPane.getChildren().add(commaLabel);
//                }
//            }
//        }
//
//        System.out.println("fill" + hotel.getId());
//
//        // Vérifier si deleteButton est null
//        if (deleteButton == null) {
//            System.out.println("Button is null");
//        } else {
//            deleteButton.setUserData(hotel.getId());
//            System.out.println("kk" + deleteButton.getUserData());
//        }
//    }
//
//
//
//
//    @FXML
//    private void addHotel(ActionEvent event) {
//        try {
//            // Récupérer les données du formulaire
//            String name = nameField.getText();
//            String link = linkField.getText();
//            double latitude = Double.parseDouble(latitudeField.getText());
//            double longitude = Double.parseDouble(longitudeField.getText());
//            String checkInTime = checkInTimeField.getText();
//            String checkOutTime = checkOutTimeField.getText();
//            double lowestRate = Double.parseDouble(lowestRateField.getText());
//            double overallRating = Double.parseDouble(overallRatingField.getText());
//            int reviews = Integer.parseInt(reviewsField.getText());
//            String[] amenities = amenitiesField.getText().split(",\\s*"); // Séparer les équipements par des virgules
//
//            // Créer un objet Hotel
////            Hotel hotel = new Hotel(
////                    name,
////                    link,
////                    new Hotel.GpsCoordinates(latitude, longitude),
////                    checkInTime,
////                    checkOutTime,
////                    lowestRate,
////                    overallRating,
////                    reviews,
////                    amenities
////            );
//
//            boolean isCreated = hotelService.createHotel(hotel);
//
//            if (isCreated) {
//                showAlert(Alert.AlertType.INFORMATION, "Success", "The hotel has been added successfully.");
//                clearForm(); // Réinitialiser le formulaire
//            } else {
//                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add the hotel.");
//            }
//        } catch (NumberFormatException e) {
//            showAlert(Alert.AlertType.ERROR, "Error", "Please enter valid numeric values.");
//        } catch (Exception e) {
//            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
//        }
//    }
//
//    private void clearForm() {
//        nameField.clear();
//        linkField.clear();
//        latitudeField.clear();
//        longitudeField.clear();
//        checkInTimeField.clear();
//        checkOutTimeField.clear();
//        lowestRateField.clear();
//        overallRatingField.clear();
//        reviewsField.clear();
//        amenitiesField.clear();
//    }
//}