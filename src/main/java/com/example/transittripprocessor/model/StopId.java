package com.example.transittripprocessor.model;

import java.util.Objects;

public record StopId(String value) {

    public StopId {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
