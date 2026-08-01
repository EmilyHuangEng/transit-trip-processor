package com.example.transittripprocessor.model;

import java.time.LocalDateTime;
import java.util.Objects;

public record Tap(
        long id,
        LocalDateTime dateTimeUtc,
        TapType tapType,
        StopId stopId,
        String companyId,
        String busId,
        String pan
) {

    public Tap {
        Objects.requireNonNull(dateTimeUtc, "dateTimeUtc must not be null");
        Objects.requireNonNull(tapType, "tapType must not be null");
        Objects.requireNonNull(stopId, "stopId must not be null");
        requireText(companyId, "companyId");
        requireText(busId, "busId");
        requireText(pan, "pan");
    }

    private static void requireText(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
