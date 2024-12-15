package org.example.hotelssearch.utils;

public class RequestCounter {
    private static int requestCount = 0;

    public static synchronized void increment() {
        requestCount++;
    }

    public static int getRequestCount() {
        return requestCount;
    }

    public static synchronized void reset() {
        requestCount = 0;
    }
}
