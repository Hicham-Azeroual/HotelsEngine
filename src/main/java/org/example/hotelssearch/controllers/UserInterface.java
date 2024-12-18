package org.example.hotelssearch.controllers;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.example.hotelssearch.models.Hotel;
import org.example.hotelssearch.services.HotelService;
import org.example.hotelssearch.utils.GPSCoordinates;
import org.example.hotelssearch.utils.GetCurrentPosition;
import org.example.hotelssearch.utils.GoogleMapsGeocoding;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserInterface {

    @FXML
    public Button currentLocationButton;
    public ComboBox currencyComboBox;

    @FXML
    private VBox card;

    @FXML
    private ProgressIndicator loadingSpinner;

    @FXML
    private RadioButton rating5Stars;

    @FXML
    private RadioButton rating4To5Stars;
    @FXML
    private CheckBox airConditioningCheckBox;

    @FXML
    private CheckBox balconyCheckBox;

    @FXML
    private CheckBox kidFriendlyCheckBox;

    @FXML
    private CheckBox fireplaceCheckBox;

    @FXML
    private CheckBox fitnessCenterCheckBox;

    @FXML
    private CheckBox heatingCheckBox;

    @FXML
    private CheckBox hotTubCheckBox;

    @FXML
    private CheckBox ironingBoardCheckBox;

    @FXML
    private CheckBox kitchenCheckBox;

    @FXML
    private CheckBox microwaveCheckBox;

    @FXML
    private CheckBox outdoorGrillCheckBox;

    @FXML
    private CheckBox ovenStoveCheckBox;

    @FXML
    private CheckBox smokeFreeCheckBox;

    @FXML
    private CheckBox cableTVCheckBox;

    @FXML
    private CheckBox washerCheckBox;

    @FXML
    private CheckBox freeParkingCheckBox;

    @FXML
    private CheckBox freeWiFiCheckBox;
    @FXML
    private ProgressIndicator voiceRecognitionProgressIndicator;

    @FXML
    private RadioButton rating3To4Stars;

    @FXML
    private RadioButton rating2To3Stars;

    @FXML
    private RadioButton rating1To2Stars;

    @FXML
    private Slider priceRangeSlider;

    @FXML
    private Label maxPriceLabel;

    @FXML
    private TextField searchCityBar;

    @FXML
    private TextField searchNameBar; // New field for searching by name

    @FXML
    private Button searchButton;

    @FXML
    private CheckBox breakfastCheckBox;

    @FXML
    private CheckBox gymCheckBox;

    @FXML
    private CheckBox poolCheckBox;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private Button voiceSearchButton;

    @FXML
    private ListView<String> autocompleteListView;

    @FXML
    private void openChatbot() {
        try {
            // Load the chatbot HTML file
            WebView webView = new WebView();
            webView.getEngine().load(getClass().getResource("/fxml/iindexChat.html").toExternalForm());

            // Create a new stage for the chatbot
            Stage chatbotStage = new Stage();
            chatbotStage.setTitle("Hotel Chatbot");
            chatbotStage.setScene(new Scene(webView, 400, 600));
            chatbotStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HotelService hotelService;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 10;
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private String currentSearchTerm;
    private Float currentMinRating;
    private Float currentMaxPrice;
    private String currentCityName;
    private Double currentRadiusInKm;
    private List<String> currentAmenities;
    private String currentSortOption = "Rating"; // Default sorting option

    private Timer searchTimer;
    private final int DEBOUNCE_DELAY = 300; // 300 milliseconds

    @FXML
    private void initialize() throws Exception {
        hotelService = new HotelService();
        card.getStylesheets().add(getClass().getResource("/styles/userInterface.css").toExternalForm());

        // Initialize the autocomplete ListView
        autocompleteListView.setVisible(false);
        autocompleteListView.setManaged(false);
        autocompleteListView.setOnMouseClicked(this::handleAutocompleteSelection);

        // Add a listener to the searchNameBar to trigger autocomplete
        searchNameBar.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                showAutocompleteSuggestions(newValue);
            } else {
                autocompleteListView.setVisible(false);
            }
        });

        // Add a listener to handle key events in the searchNameBar
        searchNameBar.setOnKeyPressed(this::handleSearchNameBarKeyPress);

        // Fetch and display initial hotel data
        fetchAndDisplayHotels(currentPage, PAGE_SIZE);

        // Add a listener to the slider to update the price label
        priceRangeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                maxPriceLabel.setText(String.format("%.0f", newValue));
                debouncedExecuteSearch();
            }
        });

        // Add event listeners for rating radio buttons
        rating5Stars.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        rating4To5Stars.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        rating3To4Stars.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        rating2To3Stars.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        rating1To2Stars.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());

        // Add event listeners for amenities checkboxes
        breakfastCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        gymCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        poolCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());

        // Ajoutez les écouteurs pour les nouvelles commodités
        airConditioningCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        balconyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        kidFriendlyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        fireplaceCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        fitnessCenterCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        heatingCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        hotTubCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        ironingBoardCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        kitchenCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        microwaveCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        outdoorGrillCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        ovenStoveCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        smokeFreeCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        cableTVCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        washerCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        freeParkingCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());
        freeWiFiCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> executeSearch());

        // Add event listener for the sorting combo box
        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentSortOption = newVal;
                executeSearch();
            }
        });
        currencyComboBox.valueProperty().addListener((obs, oldCurrency, newCurrency) -> {
            if (newCurrency != null && !newCurrency.equals(oldCurrency)) {
                updateHotelPrices((String) newCurrency);
            }
        });
        // Add event handler for the search button
        searchButton.setOnAction(event -> {
            try {
                executeSearch();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Add event handler for the voice search button
        voiceSearchButton.setOnAction(event -> {
            try {
                String voiceSearchResult = performVoiceSearch();
                if (voiceSearchResult != null) {
                    processVoiceSearchResult(voiceSearchResult);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Add event handler for the current location button
        currentLocationButton.setOnAction(event -> {
            try {
                GPSCoordinates currentCoordinates = GetCurrentPosition.getLocation();
                if (currentCoordinates != null) {
                    executeSearchWithCurrentLocation(currentCoordinates);
                } else {
                    System.err.println("Failed to get current location.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        voiceRecognitionProgressIndicator.setVisible(false);
    }
    private void updateHotelPrices(String targetCurrency) {

        // Iterate through all the hotel cards in the VBox
        for (Node node : card.getChildren()) {
            if (node instanceof VBox) {
                VBox hotelCard = (VBox) node;

                // Find the price label in the hotel card
                Label priceLabel = findPriceLabel(hotelCard);
                if (priceLabel != null) {
                    // Extract the original price from the label text
                    String priceText = priceLabel.getText();
                    double originalPrice = extractPriceFromText(priceText);

                    try {
                        // Convert the price to the target currency
                        double convertedPrice = HotelService.convertCurrency(targetCurrency, originalPrice);
                        // Update the price label with the converted price
                        priceLabel.setText(String.format("%.2f %s per night", convertedPrice, targetCurrency));
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Handle conversion error (e.g., show a default message)
                        priceLabel.setText("Price not available");
                    }
                } else {
                    System.err.println("Price label not found in hotel card.");
                }
            }
        }
    }
    private Label findPriceLabel(VBox hotelCard) {
        // Search for the hotelDetailsBox within the hotelCard
        for (Node node : hotelCard.getChildren()) {
            if (node instanceof HBox) {
                HBox hotelInfoBox = (HBox) node;
                for (Node child : hotelInfoBox.getChildren()) {
                    if (child instanceof VBox) {
                        VBox hotelDetailsBox = (VBox) child;
                        for (Node detailNode : hotelDetailsBox.getChildren()) {
                            if (detailNode instanceof Label) {
                                Label label = (Label) detailNode;
                                if (label.getText().contains("per night")) {
                                    return label;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private double extractPriceFromText(String priceText) {
        try {
            // Extract the numeric part of the price
            String[] parts = priceText.split(" ");
            return Double.parseDouble(parts[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0.0; // Return 0.0 if parsing fails
        }
    }
    private void handleSearchNameBarKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.DOWN) {
            if (!autocompleteListView.isVisible()) {
                showAutocompleteSuggestions(searchNameBar.getText());
            }
            autocompleteListView.requestFocus();
            if (!autocompleteListView.getItems().isEmpty()) {
                autocompleteListView.getSelectionModel().select(0);
            }
        }
    }

    private void handleAutocompleteSelection(MouseEvent event) {
        String selectedItem = autocompleteListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            searchNameBar.setText(selectedItem);
            autocompleteListView.setVisible(false);
            executeSearch();
        }
    }

    private void showAutocompleteSuggestions(String searchTerm) {
        executorService.submit(() -> {
            try {
                List<String> suggestions = hotelService.executeAutocompleteQuery(searchTerm, 0, 10);
                Platform.runLater(() -> {
                    autocompleteListView.getItems().clear();
                    autocompleteListView.getItems().addAll(suggestions);
                    autocompleteListView.setPrefHeight(2* 24); // Adjust height based on number of suggestions
                    autocompleteListView.setVisible(true);
                    autocompleteListView.setManaged(true);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void fetchAndDisplayHotels(int from, int size) throws Exception {
        // Show the spinner before fetching data
        loadingSpinner.setVisible(true);

        // Fetch hotel data from the service
        List<Hotel> hotels = hotelService.retrieveAllHotels(from, size);

        // Populate cards with hotel data
        for (Hotel hotel : hotels) {
            VBox hotelCard = createHotelCard(hotel);
            card.getChildren().add(hotelCard);
        }

        // Add the "Load More" button dynamically
        addLoadMoreButton();

        // Hide the spinner after data is fetched and displayed
        loadingSpinner.setVisible(false);
    }

    private void addLoadMoreButton() {
        Button loadMoreButton = new Button("Load More");
        loadMoreButton.getStyleClass().add("load-more-button");
        loadMoreButton.setOnAction(event -> {
            try {
                loadMoreHotels();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        card.getChildren().add(loadMoreButton);
    }

    @FXML
    private void loadMoreHotels() throws Exception {
        currentPage++;
        List<Hotel> hotels = hotelService.executeQuery(
                currentSearchTerm,
                currentMinRating,
                currentMaxPrice,
                currentCityName,
                currentRadiusInKm,
                currentAmenities,
                currentPage * PAGE_SIZE,
                PAGE_SIZE,
                null
        );

        // Clear the existing "Load More" button before adding new hotels
        card.getChildren().removeIf(node -> node instanceof Button && ((Button) node).getText().equals("Load More"));

        // Display the new hotels
        displayHotels(hotels);

        // Update the prices of the newly added hotels with the currently selected currency
        updateHotelPrices((String) currencyComboBox.getValue());

        // Add the "Load More" button again
        addLoadMoreButton();
    }
    private VBox createHotelCard(Hotel hotel) throws Exception {
        VBox hotelCard = new VBox();
        hotelCard.getStyleClass().add("card");

        HBox hotelInfoBox = new HBox(10); // Reduce spacing between elements
        hotelInfoBox.setPrefHeight(164);
        hotelInfoBox.setPrefWidth(500);

        // Create a ListView for the image slider
        ListView<String> imageListView = new ListView<>();
        imageListView.setPrefHeight(175);
        imageListView.setPrefWidth(238);
        imageListView.setCellFactory(param -> new ImageViewCell());
        imageListView.getItems().addAll(hotel.getImages());

        VBox hotelDetailsBox = new VBox(5); // Reduce spacing between elements
        hotelDetailsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        hotelDetailsBox.setPrefHeight(200);
        hotelDetailsBox.setPrefWidth(208);

        Label nameLabel = new Label(hotel.getName());
        nameLabel.getStyleClass().add("label");

        Label priceLabel = new Label(hotel.getLowest_rate() + " USD per night"); // Ensure this label is added
        priceLabel.getStyleClass().add("small-label");

        Label ratingLabel = new Label(hotel.getOverall_rating() + " stars");
        ratingLabel.getStyleClass().add("small-label");

        HBox locationBox = new HBox(5);
        locationBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        ImageView locationIcon = new ImageView();
        locationIcon.setFitHeight(15);
        locationIcon.setFitWidth(15);
        locationIcon.getStyleClass().add("icon-view");
        loadImageAsync(locationIcon, "https://cdn-icons-png.flaticon.com/512/684/684908.png");

        // Use a TextFlow to allow text wrapping
        TextFlow locationTextFlow = new TextFlow();
        locationTextFlow.getChildren().addAll(new Text(GoogleMapsGeocoding.getDetailedLocation(hotel.getGps_coordinates())));
        locationTextFlow.setMaxWidth(200); // Set a maximum width for the text flow

        locationBox.getChildren().addAll(locationIcon, locationTextFlow);

        HBox distanceBox = new HBox(5);
        distanceBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        ImageView distanceIcon = new ImageView();
        distanceIcon.setFitHeight(15);
        distanceIcon.setFitWidth(15);
        distanceIcon.getStyleClass().add("icon-view");
        loadImageAsync(distanceIcon, "https://cdn-icons-png.flaticon.com/512/109/109613.png");
        double distance = GoogleMapsGeocoding.calculateDistance(hotel.getGps_coordinates(), GetCurrentPosition.getLocation());
        String formattedDistance = String.format("%.2f", distance); // Format the float to 2 decimal places
        Label distanceLabel = new Label(formattedDistance + " km");

        distanceLabel.getStyleClass().add("small-label");
        distanceBox.getChildren().addAll(distanceIcon, distanceLabel);

        hotelDetailsBox.getChildren().addAll(nameLabel, priceLabel, ratingLabel, locationBox, distanceBox); // Ensure priceLabel is added

        VBox amenitiesBox = new VBox(5); // Reduce spacing between elements
        amenitiesBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        amenitiesBox.setPrefHeight(200);
        amenitiesBox.setPrefWidth(277);

        Label amenitiesLabel = new Label("Amenities: " + String.join(", ", hotel.getAmenities()));
        amenitiesLabel.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        amenitiesLabel.setPrefHeight(163);
        amenitiesLabel.setPrefWidth(205);
        amenitiesLabel.getStyleClass().add("small-label");
        amenitiesLabel.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);

        Button bookNowButton = new Button("Book Now");
        bookNowButton.getStyleClass().add("book-button");

        // Add "Show in Map" button
        Button showInMapButton = new Button("Show in Map");
        showInMapButton.getStyleClass().add("map-button");
        showInMapButton.setOnAction(event -> {
            try {
                GPSCoordinates userCoordinates = GetCurrentPosition.getLocation();
                openMapPopup(userCoordinates, hotel.getGps_coordinates());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Add spacing between buttons
        HBox buttonBox = new HBox(10, bookNowButton, showInMapButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);

        amenitiesBox.getChildren().addAll(amenitiesLabel, buttonBox);

        hotelInfoBox.getChildren().addAll(imageListView, hotelDetailsBox, amenitiesBox);

        hotelCard.getChildren().add(hotelInfoBox);

        return hotelCard;
    }

    private void openMapPopup(GPSCoordinates userCoordinates, GPSCoordinates hotelCoordinates) {
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        // Load the static map.html file
        URL mapUrl = getClass().getResource("/fxml/map.html");
        if (mapUrl != null) {
            engine.load(mapUrl.toExternalForm());
        } else {
            System.err.println("map.html file not found.");
        }

        Stage mapStage = new Stage();
        mapStage.setTitle("Hotel Location");
        mapStage.setScene(new Scene(webView, 600, 400));
        mapStage.show();
    }

    private void loadImageAsync(ImageView imageView, String imageUrl) {
        executorService.submit(() -> {
            try {
                Image image = new Image(imageUrl, 15, 15, false, false);
                Platform.runLater(() -> imageView.setImage(image));
            } catch (Exception e) {
                // Handle image loading error
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleRatingSelection() {
        // Deselect all other radio buttons
        if (rating5Stars.isSelected()) {
            rating4To5Stars.setSelected(false);
            rating3To4Stars.setSelected(false);
            rating2To3Stars.setSelected(false);
            rating1To2Stars.setSelected(false);
        } else if (rating4To5Stars.isSelected()) {
            rating5Stars.setSelected(false);
            rating3To4Stars.setSelected(false);
            rating2To3Stars.setSelected(false);
            rating1To2Stars.setSelected(false);
        } else if (rating3To4Stars.isSelected()) {
            rating5Stars.setSelected(false);
            rating4To5Stars.setSelected(false);
            rating2To3Stars.setSelected(false);
            rating1To2Stars.setSelected(false);
        } else if (rating2To3Stars.isSelected()) {
            rating5Stars.setSelected(false);
            rating4To5Stars.setSelected(false);
            rating3To4Stars.setSelected(false);
            rating1To2Stars.setSelected(false);
        } else if (rating1To2Stars.isSelected()) {
            rating5Stars.setSelected(false);
            rating4To5Stars.setSelected(false);
            rating3To4Stars.setSelected(false);
            rating2To3Stars.setSelected(false);
        }
    }

    private static class ImageViewCell extends javafx.scene.control.ListCell<String> {
        private final ImageView imageView = new ImageView();
        private final HBox container = new HBox();
        private final ProgressIndicator loadingSpinner = new ProgressIndicator();

        public ImageViewCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setPrefSize(238, 175);
            loadingSpinner.setVisible(false);
            loadingSpinner.getStyleClass().add("loading-spinner"); // Add style class
            container.setAlignment(javafx.geometry.Pos.CENTER);
            container.getChildren().add(loadingSpinner);
        }

        @Override
        protected void updateItem(String imageUrl, boolean empty) {
            super.updateItem(imageUrl, empty);
            if (empty || imageUrl == null) {
                setGraphic(null);
            } else {
                // Show the spinner before loading the image
                setGraphic(container);
                loadingSpinner.setVisible(true);

                loadImageAsync(imageView, imageUrl);
            }
        }

        private void loadImageAsync(ImageView imageView, String imageUrl) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> {
                try {
                    Image image = new Image(imageUrl, 238, 175, false, false);
                    Platform.runLater(() -> {
                        imageView.setImage(image);
                        // Hide the spinner and show the image
                        loadingSpinner.setVisible(false);
                        setGraphic(imageView);
                    });
                } catch (Exception e) {
                    // Handle image loading error
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        // Hide the spinner on error
                        loadingSpinner.setVisible(false);
                        setGraphic(null);
                    });
                }
            });
        }
    }



    private void executeSearch() {
        // Capture user input
        currentSearchTerm = searchNameBar.getText();
        currentCityName = searchCityBar.getText();
        currentMinRating = getSelectedRating();
        currentMaxPrice = (float) priceRangeSlider.getValue();
        currentAmenities = getSelectedAmenities();

        // Clear existing results and show spinner
        card.getChildren().clear();
        loadingSpinner.setVisible(true);

        // Execute the query
        executorService.submit(() -> {
            try {
                List<Hotel> hotels = hotelService.executeQuery(
                        currentSearchTerm,
                        currentMinRating,
                        currentMaxPrice,
                        currentCityName,
                        currentRadiusInKm,
                        currentAmenities,
                        0,
                        PAGE_SIZE,
                        null
                );

                // Send search events to Kafka


                hotels = hotelService.sortHotels(hotels, currentSortOption); // Sort the hotels
                List<Hotel> finalHotels = hotels;
                Platform.runLater(() -> {
                    // Hide the spinner after data is fetched and displayed
                    loadingSpinner.setVisible(false);
                    // Display the results
                    displayHotels(finalHotels);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> loadingSpinner.setVisible(false));
            }
        });
    }
    private void debouncedExecuteSearch() {
        if (searchTimer != null) {
            searchTimer.cancel();
        }
        searchTimer = new Timer();
        searchTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(UserInterface.this::executeSearch);
            }
        }, DEBOUNCE_DELAY);
    }

    private Float getSelectedRating() {
        if (rating5Stars.isSelected()) {
            return 5.0f;
        } else if (rating4To5Stars.isSelected()) {
            return 4.0f;
        } else if (rating3To4Stars.isSelected()) {
            return 3.0f;
        } else if (rating2To3Stars.isSelected()) {
            return 2.0f;
        } else if (rating1To2Stars.isSelected()) {
            return 1.0f;
        }
        return null;
    }

    private List<String> getSelectedAmenities() {
        List<String> amenities = new ArrayList<>();
        if (breakfastCheckBox.isSelected()) {
            amenities.add("Breakfast");
        }
        if (gymCheckBox.isSelected()) {
            amenities.add("Gym");
        }
        if (poolCheckBox.isSelected()) {
            amenities.add("Pool");
        }
        // Ajoutez les nouvelles commodités ici
        if (airConditioningCheckBox.isSelected()) {
            amenities.add("Air conditioning");
        }
        if (balconyCheckBox.isSelected()) {
            amenities.add("Balcony");
        }
        if (kidFriendlyCheckBox.isSelected()) {
            amenities.add("Kid-friendly");
        }
        if (fireplaceCheckBox.isSelected()) {
            amenities.add("Fireplace");
        }
        if (fitnessCenterCheckBox.isSelected()) {
            amenities.add("Fitness center");
        }
        if (heatingCheckBox.isSelected()) {
            amenities.add("Heating");
        }
        if (hotTubCheckBox.isSelected()) {
            amenities.add("Hot tub");
        }
        if (ironingBoardCheckBox.isSelected()) {
            amenities.add("Ironing board");
        }
        if (kitchenCheckBox.isSelected()) {
            amenities.add("Kitchen");
        }
        if (microwaveCheckBox.isSelected()) {
            amenities.add("Microwave");
        }
        if (outdoorGrillCheckBox.isSelected()) {
            amenities.add("Outdoor grill");
        }
        if (ovenStoveCheckBox.isSelected()) {
            amenities.add("Oven stove");
        }
        if (smokeFreeCheckBox.isSelected()) {
            amenities.add("Smoke-free");
        }
        if (cableTVCheckBox.isSelected()) {
            amenities.add("Cable TV");
        }
        if (washerCheckBox.isSelected()) {
            amenities.add("Washer");
        }
        if (freeParkingCheckBox.isSelected()) {
            amenities.add("Free parking");
        }
        if (freeWiFiCheckBox.isSelected()) {
            amenities.add("Free Wi-Fi");
        }
        return amenities;
    }
    private void displayHotels(List<Hotel> hotels) {
        for (Hotel hotel : hotels) {
            try {
                VBox hotelCard = createHotelCard(hotel);
                card.getChildren().add(hotelCard);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Re-add the "Load More" button after displaying the new hotels
        addLoadMoreButton();
    }

    private String performVoiceSearch() throws Exception {
        // Run the Python script
        ProcessBuilder processBuilder = new ProcessBuilder("python", "speech_recognition_script.py");
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // Wait for the process to complete
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            // Read the JSON file
            File jsonFile = new File("voice_recognition_result.json");
            try (FileReader reader = new FileReader(jsonFile)) {
                JsonObject jsonResult = JsonParser.parseReader(reader).getAsJsonObject();
                if (jsonResult.has("recognized_text")) {
                    return jsonResult.get("recognized_text").getAsString();
                } else if (jsonResult.has("error")) {
                    System.err.println("Error in voice recognition: " + jsonResult.get("error").getAsString());
                }
            } catch (JsonSyntaxException | IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Voice search failed with exit code: " + exitCode);
        }
        return null;
    }

    @FXML
    private void processVoiceSearchResult(String result) {
        try {
            // Parse the JSON output from the Python script
            JsonObject jsonResult = JsonParser.parseReader(new FileReader("voice_recognition_result.json")).getAsJsonObject();

            // Check if there was an error in the recognition process
            if (jsonResult.has("error")) {
                System.err.println("Error in voice recognition: " + jsonResult.get("error").getAsString());
                return;
            }

            // Extract the recognized text
            String recognizedText = jsonResult.get("recognized_text").getAsString();
            System.out.println("Recognized Text: " + recognizedText);

            // Extract the extracted_info object
            JsonObject extractedInfo = jsonResult.get("extracted_info").getAsJsonObject();

            // Initialize variables to hold the parameters
            String cityName = null;
            String hotelName = null;

            // Extract city and hotel names from the extracted_info
            if (extractedInfo.has("city")) {
                cityName = extractedInfo.get("city").getAsString();
            }
            if (extractedInfo.has("hotel")) {
                hotelName = extractedInfo.get("hotel").getAsString();
            }

            // Set the hotel name in the searchNameBar input
            searchNameBar.setText(hotelName);

            // Perform the search with the extracted parameters
            if (cityName != null || hotelName != null) {
                executeSearchWithParameters(cityName, null, null, null);
            } else {
                System.err.println("Could not extract valid parameters from the recognized text.");
            }
        } catch (Exception e) {
            System.err.println("Error processing voice search result: " + e.getMessage());
        }
    }

    private void executeSearchWithParameters(String cityName, Float rating, Float price, List<String> amenities) {
        // Capture user input
        currentSearchTerm = searchNameBar.getText(); // Use the search by name field
        currentMinRating = rating;
        currentMaxPrice = price;
        currentCityName = cityName;
        currentRadiusInKm = 100.0; // Default radius
        currentAmenities = amenities;

        // Clear existing results and show spinner
        card.getChildren().clear();
        loadingSpinner.setVisible(true);

        // Execute the query
        executorService.submit(() -> {
            try {
                List<Hotel> hotels = hotelService.executeQuery(currentSearchTerm, currentMinRating, currentMaxPrice, currentCityName, currentRadiusInKm, currentAmenities, 0, PAGE_SIZE, null);
                hotels = hotelService.sortHotels(hotels, currentSortOption); // Sort the hotels
                List<Hotel> finalHotels = hotels;
                Platform.runLater(() -> {
                    // Hide the spinner after data is fetched and displayed
                    loadingSpinner.setVisible(false);
                    // Display the results
                    displayHotels(finalHotels);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> loadingSpinner.setVisible(false));
            }
        });
    }

    private void executeSearchWithCurrentLocation(GPSCoordinates currentCoordinates) {
        // Capture user input
        currentSearchTerm = searchNameBar.getText(); // Use the search by name field
        currentMinRating = getSelectedRating();
        currentMaxPrice = (float) priceRangeSlider.getValue();
        currentCityName = null; // Clear the city name as we are using current location
        currentRadiusInKm = 100.0; // Default radius
        currentAmenities = getSelectedAmenities();

        // Clear existing results and show spinner
        card.getChildren().clear();
        loadingSpinner.setVisible(true);

        // Execute the query
        executorService.submit(() -> {
            try {
                List<Hotel> hotels = hotelService.executeQuery(currentSearchTerm, currentMinRating, currentMaxPrice, currentCityName, currentRadiusInKm, currentAmenities, 0, PAGE_SIZE, currentCoordinates);
                hotels = hotelService.sortHotels(hotels, currentSortOption); // Sort the hotels
                List<Hotel> finalHotels = hotels;
                Platform.runLater(() -> {
                    // Hide the spinner after data is fetched and displayed
                    loadingSpinner.setVisible(false);
                    // Display the results
                    displayHotels(finalHotels);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> loadingSpinner.setVisible(false));
            }
        });
    }

    @FXML
    private void handleCurrentLocationButtonAction() {
        try {
            GPSCoordinates currentCoordinates = GetCurrentPosition.getLocation();
            if (currentCoordinates != null) {
                executeSearchWithCurrentLocation(currentCoordinates);
            } else {
                System.err.println("Failed to get current location.");
            }
        } catch (Exception e) {
            e.printStackTrace();
      }
}





}
