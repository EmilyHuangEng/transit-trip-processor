package com.example.transittripprocessor.service;

import com.example.transittripprocessor.model.StopId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static com.example.transittripprocessor.model.StopId.STOP_1;
import static com.example.transittripprocessor.model.StopId.STOP_2;
import static com.example.transittripprocessor.model.StopId.STOP_3;
import static com.example.transittripprocessor.service.FareCalculator.STOP_1_STOP_2_FARE;
import static com.example.transittripprocessor.service.FareCalculator.STOP_1_STOP_3_FARE;
import static com.example.transittripprocessor.service.FareCalculator.STOP_2_STOP_3_FARE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FareCalculatorTest {

    private final FareCalculator calculator = new FareCalculator();

    @ParameterizedTest
    @MethodSource("faresInBothDirections")
    void returnsTheFareForTravelInEitherDirection(
            StopId from,
            StopId to,
            BigDecimal expectedFare
    ) {
        assertEquals(expectedFare, calculator.fareBetween(from, to));
    }

    private static Stream<Arguments> faresInBothDirections() {
        return Stream.of(
                Arguments.of(STOP_1, STOP_2, STOP_1_STOP_2_FARE),
                Arguments.of(STOP_2, STOP_1, STOP_1_STOP_2_FARE),
                Arguments.of(STOP_2, STOP_3, STOP_2_STOP_3_FARE),
                Arguments.of(STOP_3, STOP_2, STOP_2_STOP_3_FARE),
                Arguments.of(STOP_1, STOP_3, STOP_1_STOP_3_FARE),
                Arguments.of(STOP_3, STOP_1, STOP_1_STOP_3_FARE)
        );
    }

    @Test
    void returnsZeroForTravelEndingAtTheStartingStop() {
        assertEquals(BigDecimal.ZERO, calculator.fareBetween(STOP_1, STOP_1));
    }

    @ParameterizedTest
    @MethodSource("maximumFares")
    void returnsTheMaximumPossibleFareFromAStop(
            StopId from,
            BigDecimal expectedFare
    ) {
        assertEquals(expectedFare, calculator.maximumFareFrom(from));
    }

    private static Stream<Arguments> maximumFares() {
        return Stream.of(
                Arguments.of(STOP_1, STOP_1_STOP_3_FARE),
                Arguments.of(STOP_2, STOP_2_STOP_3_FARE),
                Arguments.of(STOP_3, STOP_1_STOP_3_FARE)
        );
    }
}
