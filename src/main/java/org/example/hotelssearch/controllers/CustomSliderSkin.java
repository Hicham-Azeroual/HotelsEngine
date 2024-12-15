package org.example.hotelssearch.controllers;

import javafx.scene.control.Skin;
import javafx.scene.control.Slider;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class CustomSliderSkin extends SkinBase<Slider> implements Skin<Slider> {

    private final Rectangle track;
    private final Rectangle filledTrack;

    public CustomSliderSkin(Slider slider) {
        super(slider);

        // Create the track
        track = new Rectangle();
        track.setFill(Color.LIGHTGRAY);

        // Create the filled track
        filledTrack = new Rectangle();
        filledTrack.setFill(Color.BLUE);

        // Add the track and filled track to the slider
        getChildren().addAll(track, filledTrack);

        // Update the track and filled track sizes
        slider.valueProperty().addListener((observable, oldValue, newValue) -> updateTrack(slider));
        updateTrack(slider);
    }

    private void updateTrack(Slider slider) {
        double width = slider.getWidth();
        double height = slider.getHeight();
        double thumbWidth = 10; // Adjust as needed
        double thumbHeight = 10; // Adjust as needed

        track.setWidth(width);
        track.setHeight(height);

        double filledWidth = (width - thumbWidth) * ((slider.getValue() - slider.getMin()) / (slider.getMax() - slider.getMin()));
        filledTrack.setWidth(filledWidth + thumbWidth / 2);
        filledTrack.setHeight(height);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        track.setLayoutX(x);
        track.setLayoutY(y);
        filledTrack.setLayoutX(x);
        filledTrack.setLayoutY(y);
    }
}