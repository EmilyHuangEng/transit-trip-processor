package com.example.transittripprocessor.service;

import com.example.transittripprocessor.model.StopId;
import com.example.transittripprocessor.model.StopPair;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import static com.example.transittripprocessor.model.StopId.STOP_1;
import static com.example.transittripprocessor.model.StopId.STOP_2;
import static com.example.transittripprocessor.model.StopId.STOP_3;

@Service
public class FareCalculator {

    static final BigDecimal STOP_1_STOP_2_FARE = new BigDecimal("3.25");
    static final BigDecimal STOP_2_STOP_3_FARE = new BigDecimal("5.50");
    static final BigDecimal STOP_1_STOP_3_FARE = new BigDecimal("7.30");

        private static final Map<StopPair, BigDecimal> FARES = Map.of(
            new StopPair(STOP_1, STOP_2), STOP_1_STOP_2_FARE,
            new StopPair(STOP_2, STOP_3), STOP_2_STOP_3_FARE,
            new StopPair(STOP_1, STOP_3), STOP_1_STOP_3_FARE
    );

    public BigDecimal fareBetween(StopId from, StopId to) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");

        if (from == to) {
            return BigDecimal.ZERO;
        }

        BigDecimal fare = FARES.get(new StopPair(from, to));
        if (fare == null) {
            throw new IllegalArgumentException(
                    "No fare configured between " + from + " and " + to
            );
        }
        return fare;
    }

    public BigDecimal maximumFareFrom(StopId from) {
        Objects.requireNonNull(from, "from must not be null");

        return FARES.entrySet().stream()
                .filter(entry -> entry.getKey().includes(from))
                .map(Map.Entry::getValue)
                .max(BigDecimal::compareTo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No fare configured from " + from
                ));
    }

}
