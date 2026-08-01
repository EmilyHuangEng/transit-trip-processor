package com.example.transittripprocessor.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StopIdTest {

    @ParameterizedTest
    @MethodSource("supportedCsvValues")
    void parsesCsvValuesIgnoringCaseAndSurroundingSpaces(
            String csvValue,
            StopId expected
    ) {
        assertEquals(expected, StopId.fromCsvValue(csvValue));
    }

    private static Stream<Arguments> supportedCsvValues() {
        return Stream.of(
                Arguments.of("Stop1", StopId.STOP_1),
                Arguments.of("stop1", StopId.STOP_1),
                Arguments.of("STOP1", StopId.STOP_1),
                Arguments.of(" Stop2 ", StopId.STOP_2),
                Arguments.of("Stop3", StopId.STOP_3)
        );
    }

    @ParameterizedTest
    @EnumSource(StopId.class)
    void convertsEveryStopToCsvAndBack(StopId stopId) {
        assertEquals(stopId, StopId.fromCsvValue(stopId.csvValue()));
    }

    @Test
    void rejectsAnUnknownCsvValue() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> StopId.fromCsvValue("Stop4")
        );

        assertEquals("Unknown stop ID: Stop4", exception.getMessage());
    }
}
