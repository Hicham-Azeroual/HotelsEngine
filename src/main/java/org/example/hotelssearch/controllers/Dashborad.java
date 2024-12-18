package org.example.hotelssearch.controllers;

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.util.Pair;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.example.hotelssearch.models.Hotel;
import org.example.hotelssearch.models.SessionManager;
import org.example.hotelssearch.models.User;
import org.example.hotelssearch.services.HotelService;
import org.example.hotelssearch.utils.GoogleMapsGeocoding;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Dashborad implements Initializable {
    private static final int PAGE_SIZE = 10; // Number of hotels per page
    private int currentPage = 0; // Current page index

    @FXML
    private Label usernameLabel;

    @FXML
    private Label NumberofHotels;

    @FXML
    private Label numOfReviws;

    @FXML
    private Label numOfUsers;

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
    private TextField imageUrlField;

    @FXML
    private TextField nameField1;

    @FXML
    private TextField linkField1;

    @FXML
    private TextField latitudeField1;

    @FXML
    private TextField longitudeField1;

    @FXML
    private TextField checkInTimeField1;

    @FXML
    private TextField checkOutTimeField1;

    @FXML
    private TextField lowestRateField1;

    @FXML
    private TextField overallRatingField1;

    @FXML
    private TextField reviewsField1;

    @FXML
    private TextArea amenitiesField1;

    @FXML
    private TextField imageUrlField1;

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
    private Button createHotel;

    @FXML
    private AnchorPane AddHotel;

    @FXML
    private Button loadMoreButton;

    @FXML
    private AnchorPane UpdateHotel;

    @FXML
    private TextField searchBar;

    private String hotelId;


    private final HotelService hotelService = new HotelService();

    @FXML
    private void showDashboard() {
        updateActiveButton(dashboardButton);
        scrollpane2.setVisible(true);
        scrollpane1.setVisible(false);
        AddHotel.setVisible(false);
        UpdateHotel.setVisible(false);
    }

    @FXML
    private void showCreateHotel(ActionEvent event) {
        scrollpane2.setVisible(false);
        scrollpane1.setVisible(false);
        AddHotel.setVisible(true);
        UpdateHotel.setVisible(false);
    }


    @FXML
    private void showManage() {
        updateActiveButton(manageButton);
        dashboardButton.getStyleClass().add("card");
        scrollpane2.setVisible(false);
        scrollpane1.setVisible(true);
        UpdateHotel.setVisible(false);
        AddHotel.setVisible(false);

        // Show loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        container.getChildren().add(loadingIndicator);

        // Execute the retrieval task in a separate thread
        Task<List<Hotel>> task = new Task<>() {
            @Override
            protected List<Hotel> call() throws Exception {
                currentPage = 0; // Réinitialiser la page actuelle
                int from = currentPage * PAGE_SIZE; // Index de départ
                int size = PAGE_SIZE; // Nombre d'hôtels à récupérer
                return hotelService.retrieveAllHotels(from, size); // Récupérer les hôtels
            }
        };

        task.setOnSucceeded(event -> {
            container.getChildren().remove(loadingIndicator); // Remove loading indicator
            List<Hotel> hotels = task.getValue(); // Récupérer les hôtels récupérés
            displayHotels(hotels); // Afficher les hôtels
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
    private void search(ActionEvent event) {
        // Récupérer le terme de recherche depuis la Text Field
        String searchTerm = searchBar.getText().trim();

        // Valider le terme de recherche
        if (searchTerm.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter a search term.");
            return;
        }

        // Construire la requête de recherche
        SearchRequest searchRequest = buildSearchRequest(searchTerm);

        // Exécuter la requête de recherche
        List<Hotel> searchResults = hotelService.executeSearch(searchRequest);

        // Afficher les résultats de la recherche
        displayHotels(searchResults);
    }
    private SearchRequest buildSearchRequest(String searchTerm) {
        return SearchRequest.of(s -> s
                .index("hotels") // Nom de l'index Elasticsearch
                .query(q -> q
                        .multiMatch(mm -> mm
                                .query(searchTerm) // Terme de recherche
                                .fields("name", "location", "amenities") // Champs sur lesquels effectuer la recherche
                                .type(TextQueryType.BestFields) // Stratégie de recherche
                        )
                )
        );
    }

    public void showUpdateForm(Hotel hotel) {
        this.hotelId = hotel.get_id();
        System.out.println(hotelId + "m");

        // Préremplir le formulaire avec les données de l'hôtel
        nameField1.setText(hotel.getName());
        System.out.println(hotel.get_id()  + hotel.getName());
        linkField1.setText(hotel.getLink()); // Utiliser getLink() au lieu de getWebsite()
        latitudeField1.setText(String.valueOf(hotel.getGps_coordinates().getLatitude()));
        longitudeField1.setText(String.valueOf(hotel.getGps_coordinates().getLongitude()));
        checkInTimeField1.setText(hotel.getCheck_in_time());
        checkOutTimeField1.setText(hotel.getCheck_out_time());
        lowestRateField1.setText(String.valueOf(hotel.getLowest_rate()));
        overallRatingField1.setText(String.valueOf(hotel.getOverall_rating()));
        reviewsField1.setText(String.valueOf(hotel.getReviews()));
        amenitiesField1.setText(String.join(", ", hotel.getAmenities()));
        System.out.println(hotel.getImages()[0]);
        imageUrlField1.setText(hotel.getImages()[0]);
        // Afficher le formulaire de mise à jour
        UpdateHotel.setVisible(true);
        scrollpane1.setVisible(false);
        scrollpane2.setVisible(false);
        AddHotel.setVisible(false);
    }

    @FXML
    private void signOut() {
        updateActiveButton(signOutButton);
        SessionManager.logout();
        System.out.println(SessionManager.getCurrentUser());
        try {
            // Charger la page de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hello-view.fxml"));
            Parent root = loader.load();

            // Récupérer la scène actuelle
            Scene currentScene = signOutButton.getScene();

            // Remplacer le contenu de la scène par la page de connexion
            currentScene.setRoot(root);
            currentScene.getWindow().sizeToScene();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load login page.");
        }
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
        container.getChildren().removeIf(node -> !(node instanceof Button));


        for (Hotel hotel : hotels) {
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

        // Gérer la visibilité du bouton "Load More"
        if (hotels.size() == PAGE_SIZE) {
            loadMoreButton.setVisible(true); // Afficher le bouton si des hôtels sont disponibles
        } else {
            loadMoreButton.setVisible(false); // Masquer le bouton si tous les hôtels sont chargés
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    public void reloadHotels() {
        container.getChildren().clear(); // Effacer le conteneur
        currentPage = 0; // Réinitialiser la page actuelle

        // Afficher l'indicateur de chargement
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        container.getChildren().add(loadingIndicator);

        // Exécuter la tâche de récupération des hôtels dans un thread séparé
        Task<List<Hotel>> task = new Task<>() {
            @Override
            protected List<Hotel> call() throws Exception {
                int from = currentPage * PAGE_SIZE; // Index de départ
                int size = PAGE_SIZE; // Nombre d'hôtels à récupérer
                return hotelService.retrieveAllHotels(from, size); // Récupérer les hôtels de la première page
            }
        };

        task.setOnSucceeded(event -> {
            container.getChildren().remove(loadingIndicator); // Retirer l'indicateur de chargement
            List<Hotel> hotels = task.getValue(); // Récupérer les hôtels récupérés
            displayHotels(hotels); // Afficher les hôtels
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            container.getChildren().remove(loadingIndicator); // Retirer l'indicateur de chargement
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
        Button updateButton = (Button) card.lookup("#updateButton");

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
        if (updateButton != null) {
            updateButton.setUserData(hotel.get_id());
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
            String imageUrl = imageUrlField.getText(); // Récupérer l'URL de l'image

            // Vérifier si l'URL de l'image est vide ou non
            if (imageUrl.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Image URL is empty. A default image will be used.");
                imageUrl = "default.jpg"; // Utiliser une image par défaut si l'URL est vide
            }

            Hotel hotel = new Hotel(
                    name,
                    link,
                    new Hotel.GpsCoordinates(latitude, longitude),
                    checkInTime,
                    checkOutTime,
                    lowestRate,
                    new String[]{imageUrl}, // Ajouter l'URL de l'image
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
                AddHotel.setVisible(false);
                scrollpane2.setVisible(true);
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

        // Show loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        container.getChildren().add(loadingIndicator);

        // Execute the retrieval task in a separate thread
        Task<List<Hotel>> task = new Task<>() {
            @Override
            protected List<Hotel> call() throws Exception {
                int from = currentPage * PAGE_SIZE; // Index de départ
                int size = PAGE_SIZE; // Nombre d'hôtels à récupérer
                return hotelService.retrieveAllHotels(from, size); // Récupérer les hôtels
            }
        };

        task.setOnSucceeded(event -> {
            container.getChildren().remove(loadingIndicator); // Remove loading indicator
            List<Hotel> hotels = task.getValue(); // Récupérer les hôtels récupérés
            displayHotels(hotels); // Afficher les hôtels
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
    private void updateHotel(ActionEvent event) {
        // Récupérer les données du formulaire
        String name = nameField1.getText();
        String link = linkField1.getText();
        float latitude = Float.parseFloat(latitudeField1.getText());
        float longitude = Float.parseFloat(longitudeField1.getText());
        String checkInTime = checkInTimeField1.getText();
        String checkOutTime = checkOutTimeField1.getText();
        float lowestRate = Float.parseFloat(lowestRateField1.getText());
        float overallRating = Float.parseFloat(overallRatingField1.getText());
        int reviews = Integer.parseInt(reviewsField1.getText());
        String[] amenities = amenitiesField1.getText().split(", ");
        String[] image = new String[]{imageUrlField1.getText()};

        System.out.println(hotelId + " " + reviews);

        // Créer un nouvel objet Hotel avec les données mises à jour
        Hotel updatedHotel = new Hotel(
                name, link, new Hotel.GpsCoordinates(latitude, longitude),
                checkInTime, checkOutTime, lowestRate, image, overallRating, reviews, amenities,hotelId
        );

        // Appeler le service pour mettre à jour l'hôtel dans Elasticsearch
        boolean isUpdated = hotelService.updateHotel(hotelId,updatedHotel);

        if (isUpdated) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Hotel updated successfully!");
            UpdateHotel.setVisible(false); // Masquer le formulaire après la mise à jour
            scrollpane2.setVisible(true);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update hotel.");
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        User currentUser = SessionManager.getCurrentUser();


        System.out.println(SessionManager.getCurrentUser());
        System.out.println(currentUser);

        if (currentUser != null) {
            System.out.println("ok");
            // Afficher le nom d'utilisateur dans le label
            usernameLabel.setText(currentUser.getUsername());
        } else {
            // Si l'utilisateur n'est pas connecté, afficher un message par défaut
            usernameLabel.setText("Utilisateur non connecté");
        }
        // Créer une tâche pour récupérer les données des trois labels
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Récupérer le nombre total d'hôtels
                Long numberOfHotels = hotelService.getTotalNumberOfHotels();

                // Récupérer le nombre total de reviews
                Long numberOfReviews = hotelService.getTotalNumberOfReviews(); // Assurez-vous que cette méthode existe

                // Récupérer le nombre total d'utilisateurs
                Long numberOfUsers = hotelService.getTotalNumberOfUsers(); // Assurez-vous que cette méthode existe

                // Mettre à jour les labels dans le thread JavaFX (UI thread)
                updateLabel(NumberofHotels, numberOfHotels);
                updateLabel(numOfReviws, numberOfReviews);
                updateLabel(numOfUsers, numberOfUsers);

                return null;
            }
        };

        // Lancer la tâche dans un thread séparé
        new Thread(task).start();
    }

    // Méthode pour mettre à jour un Label dans le thread JavaFX (UI thread)
    private void updateLabel(Label label, Long value) {
        Platform.runLater(() -> label.setText(String.valueOf(value)));
    }
}
