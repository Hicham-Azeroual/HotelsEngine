package org.example.hotelssearch.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GPSCoordinates {
    @JsonProperty("lat")
    private double latitude;

    @JsonProperty("lng")
    private double longitude;

    public GPSCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "Latitude: " + latitude + ", Longitude: " + longitude;
    }
}