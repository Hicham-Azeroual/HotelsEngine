package org.example.hotelssearch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.hotelssearch.utils.GPSCoordinates;
import org.example.hotelssearch.utils.GoogleMapsGeocoding;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Hotel {

    private String name;
    private String link;
    private GpsCoordinates gps_coordinates;
    private String check_in_time;
    private String check_out_time;
    private float lowest_rate;
    private String[] images;
    private float overall_rating;
    private int reviews;
    private String[] amenities;
    public String _id;

    // Constructeur
    public Hotel(String name, String link, GpsCoordinates gps_coordinates, String check_in_time, String check_out_time,
                 float lowest_rate, String[] images, float overall_rating, int reviews, String[] amenities, String _id) {
        this.name = name;
        this.link = link;
        this.gps_coordinates = gps_coordinates;
        this.check_in_time = check_in_time;
        this.check_out_time = check_out_time;
        this.lowest_rate = lowest_rate;
        this.images = images;
        this.overall_rating = overall_rating;
        this.reviews = reviews;
        this.amenities = amenities;
        this._id = _id;
    }

    // Constructeur par défaut (sans arguments)
    public Hotel() {
        // Initialisation par défaut
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public GPSCoordinates getGps_coordinates() {
        return new GPSCoordinates(gps_coordinates.getLatitude(), gps_coordinates.getLongitude());
    }

    public void setGps_coordinates(GpsCoordinates gps_coordinates) {
        this.gps_coordinates = gps_coordinates;
    }

    public String getCheck_in_time() {
        return check_in_time;
    }

    public void setCheck_in_time(String check_in_time) {
        this.check_in_time = check_in_time;
    }

    public String getCheck_out_time() {
        return check_out_time;
    }

    public void setCheck_out_time(String check_out_time) {
        this.check_out_time = check_out_time;
    }

    public float getLowest_rate() {
        return lowest_rate;
    }

    public void setLowest_rate(float lowest_rate) {
        this.lowest_rate = lowest_rate;
    }

    public String[] getImages() {
        return images;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    public float getOverall_rating() {
        return overall_rating;
    }

    public void setOverall_rating(float overall_rating) {
        this.overall_rating = overall_rating;
    }

    public int getReviews() {
        return reviews;
    }

    public void setReviews(int reviews) {
        this.reviews = reviews;
    }

    public String[] getAmenities() {
        return amenities;
    }

    public void setAmenities(String[] amenities) {
        this.amenities = amenities;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }
    public Map<String, Object> toElasticsearchFormat() {
        Map<String, Object> hotelMap = new HashMap<>();
        hotelMap.put("name", this.name);
        hotelMap.put("link", this.link);
        hotelMap.put("check_in_time", this.check_in_time);
        hotelMap.put("check_out_time", this.check_out_time);
        hotelMap.put("lowest_rate", this.lowest_rate);
        hotelMap.put("images", this.images);
        hotelMap.put("overall_rating", this.overall_rating);
        hotelMap.put("reviews", this.reviews);
        hotelMap.put("amenities", this.amenities);

        // Convertir gps_coordinates en un format compatible avec Elasticsearch
        if (this.gps_coordinates != null) {
            Map<String, Double> geoPoint = new HashMap<>();
            geoPoint.put("lat", this.gps_coordinates.getLatitude());
            geoPoint.put("lon", this.gps_coordinates.getLongitude());
            hotelMap.put("gps_coordinates", geoPoint);
        }

        return hotelMap;
    }

    // toString method for easy printing
    @Override
    public String toString() {
        return "Hotel{" +
                "_id='" + _id + '\'' +
                ", name='" + name + '\'' +
                ", link='" + (link != null ? link : "N/A") + '\'' +
                ", gps_coordinates=" + gps_coordinates +
                ", check_in_time='" + (check_in_time != null ? check_in_time : "N/A") + '\'' +
                ", check_out_time='" + (check_out_time != null ? check_out_time : "N/A") + '\'' +
                ", lowest_rate=" + lowest_rate +
                ", overall_rating=" + overall_rating +
                ", reviews=" + reviews +
                ", amenities=" + (amenities != null ? String.join(", ", amenities) : "N/A") +
                '}';
    }

    // Method to calculate distance between hotel and current location
    public double calculateDistanceTo(GPSCoordinates currentLocation) {
        return GoogleMapsGeocoding.calculateDistance(currentLocation, getGps_coordinates());
    }

    // Inner class for GPS coordinates
    public static class GpsCoordinates {
        @JsonProperty("lat")
        private double latitude;

        @JsonProperty("lon")
        private double longitude;

        // Default constructor
        public GpsCoordinates() {
        }

        // Parameterized constructor
        public GpsCoordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        // Getters and setters
        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        @Override
        public String toString() {
            return "GpsCoordinates{" +
                    "latitude=" + latitude +
                    ", longitude=" + longitude +
                    '}';
        }
    }}