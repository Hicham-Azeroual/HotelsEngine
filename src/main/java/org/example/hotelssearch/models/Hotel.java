package org.example.hotelssearch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Hotel {
    private String id;
    private String name;
    private String link;
    private GpsCoordinates gps_coordinates;
    private String check_in_time;
    private String check_out_time;
    private double lowest_rate;
    private String[] images;
    private double overall_rating;
    private int reviews;
    private String[] amenities;
//
//    public Hotel(String name, String link, GpsCoordinates gpsCoordinates, String checkInTime, String checkOutTime, double lowestRate, double overallRating, int reviews, String[] amenities) {
//        this.name = name;
//        this.link = link;
//        this.gps_coordinates = gpsCoordinates;
//        this.check_in_time = checkInTime;
//        this.check_out_time = checkOutTime;
//        this.amenities = amenities;
//        this.lowest_rate=lowestRate;
//        this.overall_rating=overallRating;
//        this.reviews=reviews;
//    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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
    public GpsCoordinates getGps_coordinates() {
        return gps_coordinates;
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

    public double getLowest_rate() {
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

    public double getOverall_rating() {
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

    // toString method for easy printing
    @Override
    public String toString() {
        return "Hotel{" +
                "name='" + name + '\'' +
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

    // Inner class for GPS coordinates
    public static class GpsCoordinates {
        @JsonProperty("lat")
        private double latitude;

        @JsonProperty("lon")
        private double longitude;

        public GpsCoordinates(double latitude, double longitude) {
            this.latitude=latitude;
            this.longitude=longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = (float) latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = (float) longitude;
        }

        @Override
        public String toString() {
            return "GpsCoordinates{" +
                    "latitude=" + latitude +
                    ", longitude=" + longitude +
                    '}';
        }
    }
}