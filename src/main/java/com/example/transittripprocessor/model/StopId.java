package com.example.transittripprocessor.model;

import java.util.Locale;

public enum StopId {
    STOP_1("Stop1"),
    STOP_2("Stop2"),
    STOP_3("Stop3");

    private final String csvValue;

    StopId(String csvValue) {
        this.csvValue = csvValue;
    }

    public String csvValue() {
        return csvValue;
    }

    public static StopId fromCsvValue(String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "stop1" -> STOP_1;
            case "stop2" -> STOP_2;
            case "stop3" -> STOP_3;
            default -> throw new IllegalArgumentException(
                    "Unknown stop ID: " + value
            );
        };
    }
}
