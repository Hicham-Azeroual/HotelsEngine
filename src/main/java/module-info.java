module org.example.hotelssearch {
    requires javafx.controls;
    requires javafx.fxml;
    requires elasticsearch.java;
    requires org.apache.httpcomponents.httpcore;
    requires elasticsearch.rest.client;
    requires org.apache.httpcomponents.httpasyncclient;
    requires jakarta.json;
    requires java.net.http;
    requires org.json;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;
    requires jbcrypt;

    opens org.example.hotelssearch.controllers to javafx.fxml; // Allow JavaFX to access the controllers package
    exports org.example.hotelssearch.models;  // Export the models package
    exports org.example.hotelssearch.utils;

}
