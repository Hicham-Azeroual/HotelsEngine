package org.example.hotelssearch;

import org.example.hotelssearch.models.Hotel;
import org.example.hotelssearch.models.User;
import org.example.hotelssearch.services.HotelService;
import org.example.hotelssearch.services.UserService;
import org.example.hotelssearch.utils.GetCurrentPosition;

import java.util.Arrays;
import java.util.List;

public class MainApp {
    public static void main(String[] args) throws Exception {
        // Initialize the HotelService class
        HotelService hotelService = new HotelService();
        //UserService userService=new UserService();
        //User newUser = new User("hicham", "hicham@gmail.com", "hicham123");
        //System.out.println(userService.signIn("hicham","hicham123"));
        // List<String> amenities = Arrays.asList("wifi", "pool", "parking","Spa", "Bar");

        String searchTerm = "Flower Town Hotel";
        String cityName = "rabat";
        float minRating = 4.0f;
        float maxPrice = 150.0f;
        double radiusInKm = 70.0f;
        //System.out.println(hotelService.getTotalNumberOfHotels());
        //System.out.println(hotelService.getTotalNumberOfReviews());
        //System.out.println(hotelService.getNumberOfHotelsWithMissingData());
        GetCurrentPosition positionService = new GetCurrentPosition();

        // Get location data

        // Display the results
        UserService userService = new UserService();
        String searchTerm1 = "Flip";
        int from = 0;  // Pagination: starting from the first result
        int size = 10;  // Pagination: return up to 10 results

        try {
            List<String> hotels = hotelService.executeAutocompleteQuery(searchTerm1, from, size);
            hotels.forEach(System.out::println);  // Print the hotel names that match the search term
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Perform the search
        //hotelService.executeQuery(null, null, null, "rabat", radiusInKm,null,1,15);

        try {
            // Retrieve and display all hotels from Elasticsearch
            System.out.println("Retrieving all hotels from Elasticsearch...");
            //hotelService.retrieveAllHotels(15,30);
            //hotelService.searchHotelsByName("Flower Town Hotel & Spa");
            float x=134.1f;
            //hotelService.searchHotelsByRating(x);
            //hotelService.searchHotelsByMaxPrice(x);
            // hotelService.searchByCityName("rabat");
            //hotelService.getRatingDistribution();

        } catch (Exception e) {
            System.err.println("Error retrieving hotels: " + e.getMessage());
        } finally {
            // Close the Elasticsearch client after use
            hotelService.closeClient();
        }
    }}