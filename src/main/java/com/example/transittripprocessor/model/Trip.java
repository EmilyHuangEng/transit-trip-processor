package com.example.transittripprocessor.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record Trip(
        LocalDateTime started,
        LocalDateTime finished,
        long durationSecs,
        StopId fromStopId,
        StopId toStopId,
        BigDecimal chargeAmount,
        String companyId,
        String busId,
        String pan,
        TripStatus status
) {

    public Trip {
        Objects.requireNonNull(started, "started must not be null");
        Objects.requireNonNull(fromStopId, "fromStopId must not be null");
        Objects.requireNonNull(chargeAmount, "chargeAmount must not be null");
        Objects.requireNonNull(companyId, "companyId must not be null");
        Objects.requireNonNull(busId, "busId must not be null");
        Objects.requireNonNull(pan, "pan must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }
}
