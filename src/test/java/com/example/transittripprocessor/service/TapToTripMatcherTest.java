package com.example.transittripprocessor.service;

import com.example.transittripprocessor.model.StopId;
import com.example.transittripprocessor.model.Tap;
import com.example.transittripprocessor.model.TapType;
import com.example.transittripprocessor.model.Trip;
import com.example.transittripprocessor.model.TripStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.example.transittripprocessor.model.StopId.STOP_1;
import static com.example.transittripprocessor.model.StopId.STOP_2;
import static com.example.transittripprocessor.model.StopId.STOP_3;
import static com.example.transittripprocessor.model.TapType.OFF;
import static com.example.transittripprocessor.model.TapType.ON;
import static com.example.transittripprocessor.service.FareCalculator.STOP_1_STOP_2_FARE;
import static com.example.transittripprocessor.service.FareCalculator.STOP_1_STOP_3_FARE;
import static com.example.transittripprocessor.service.FareCalculator.STOP_2_STOP_3_FARE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TapToTripMatcherTest {

    private final TapToTripMatcher matcher =
            new TapToTripMatcher(new FareCalculator());

    @Test
    void createsCompletedCancelledAndIncompleteTrips() {
        List<Tap> taps = List.of(
                tap(1, "2023-01-22T13:00:00", ON, STOP_1, "Bus37", "5500"),
                tap(2, "2023-01-22T13:05:00", OFF, STOP_2, "Bus37", "5500"),
                tap(3, "2023-01-22T09:20:00", ON, STOP_3, "Bus36", "4111"),
                tap(4, "2023-01-23T08:00:00", ON, STOP_1, "Bus37", "4111"),
                tap(5, "2023-01-23T08:02:00", OFF, STOP_1, "Bus37", "4111"),
                tap(6, "2023-01-24T16:30:00", OFF, STOP_2, "Bus37", "5500")
        );

        List<Trip> trips = matcher.match(taps);

        assertEquals(List.of(
                new Trip(
                        dateTime("2023-01-22T09:20:00"),
                        null,
                        0,
                        STOP_3,
                        null,
                        STOP_1_STOP_3_FARE,
                        "Company1",
                        "Bus36",
                        "4111",
                        TripStatus.INCOMPLETE
                ),
                new Trip(
                        dateTime("2023-01-22T13:00:00"),
                        dateTime("2023-01-22T13:05:00"),
                        300,
                        STOP_1,
                        STOP_2,
                        STOP_1_STOP_2_FARE,
                        "Company1",
                        "Bus37",
                        "5500",
                        TripStatus.COMPLETED
                ),
                new Trip(
                        dateTime("2023-01-23T08:00:00"),
                        dateTime("2023-01-23T08:02:00"),
                        120,
                        STOP_1,
                        STOP_1,
                        BigDecimal.ZERO,
                        "Company1",
                        "Bus37",
                        "4111",
                        TripStatus.CANCELLED
                )
        ), trips);
    }

    @Test
    void makesThePreviousTripIncompleteWhenAnotherTapOnArrives() {
        List<Tap> taps = List.of(
                tap(1, "2023-01-22T08:00:00", ON, STOP_1, "Bus37", "4111"),
                tap(2, "2023-01-22T09:00:00", ON, STOP_2, "Bus37", "4111"),
                tap(3, "2023-01-22T10:00:00", OFF, STOP_3, "Bus37", "4111")
        );

        List<Trip> trips = matcher.match(taps);

        assertEquals(TripStatus.INCOMPLETE, trips.get(0).status());
        assertEquals(STOP_1_STOP_3_FARE, trips.get(0).chargeAmount());
        assertEquals(TripStatus.COMPLETED, trips.get(1).status());
        assertEquals(STOP_2_STOP_3_FARE, trips.get(1).chargeAmount());
    }

    @Test
    void returnsNoTripsForNoTaps() {
        assertTrue(matcher.match(List.of()).isEmpty());
    }

    @Test
    void rejectsANullTapListWithAClearMessage() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> matcher.match(null)
        );

        assertEquals("taps must not be null", exception.getMessage());
    }

    @Test
    void rejectsANullTapInsideTheListWithAClearMessage() {
        List<Tap> taps = Arrays.asList(
                tap(1, "2023-01-22T08:00:00", ON, STOP_1, "Bus37", "4111"),
                null
        );

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> matcher.match(taps)
        );

        assertEquals("taps must not contain null", exception.getMessage());
    }

    @Test
    void usesTapIdToOrderEventsWithTheSameTimestamp() {
        List<Tap> taps = List.of(
                tap(2, "2023-01-22T08:00:00", OFF, STOP_2, "Bus37", "4111"),
                tap(1, "2023-01-22T08:00:00", ON, STOP_1, "Bus37", "4111")
        );

        List<Trip> trips = matcher.match(taps);

        assertEquals(1, trips.size());
        assertEquals(TripStatus.COMPLETED, trips.getFirst().status());
        assertEquals(0, trips.getFirst().durationSecs());
    }

    @Test
    void calculatesTheFareForTheReverseTravelDirection() {
        List<Tap> taps = List.of(
                tap(1, "2023-01-22T08:00:00", ON, STOP_3, "Bus37", "4111"),
                tap(2, "2023-01-22T08:10:00", OFF, STOP_1, "Bus37", "4111")
        );

        Trip trip = matcher.match(taps).getFirst();

        assertEquals(STOP_1_STOP_3_FARE, trip.chargeAmount());
        assertEquals(600, trip.durationSecs());
        assertEquals(TripStatus.COMPLETED, trip.status());
    }

    @Test
    void ignoresAnOffTapThatHasNoMatchingOn() {
        List<Tap> taps = List.of(
                tap(1, "2023-01-22T08:00:00", OFF, STOP_2, "Bus37", "4111")
        );

        assertTrue(matcher.match(taps).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "Company1, Bus37, 9999",
            "Company1, Bus38, 4111",
            "Company2, Bus37, 4111"
    })
    void doesNotMatchTapsWhenAnIdentityFieldDiffers(
            String offCompanyId,
            String offBusId,
            String offPan
    ) {
        List<Tap> taps = List.of(
                tap(
                        1,
                        "2023-01-22T08:00:00",
                        ON,
                        STOP_1,
                        "Company1",
                        "Bus37",
                        "4111"
                ),
                tap(
                        2,
                        "2023-01-22T08:10:00",
                        OFF,
                        STOP_2,
                        offCompanyId,
                        offBusId,
                        offPan
                )
        );

        List<Trip> trips = matcher.match(taps);

        assertEquals(1, trips.size());
        assertEquals(TripStatus.INCOMPLETE, trips.getFirst().status());
        assertEquals("4111", trips.getFirst().pan());
    }

    @Test
    void matchesInterleavedTripsIndependently() {
        List<Tap> taps = List.of(
                tap(1, "2023-01-22T08:00:00", ON, STOP_1, "Bus37", "4111"),
                tap(2, "2023-01-22T08:01:00", ON, STOP_2, "Bus37", "5500"),
                tap(3, "2023-01-22T08:05:00", OFF, STOP_2, "Bus37", "4111"),
                tap(4, "2023-01-22T08:08:00", OFF, STOP_3, "Bus37", "5500")
        );

        List<Trip> trips = matcher.match(taps);

        assertEquals(2, trips.size());
        assertEquals("4111", trips.get(0).pan());
        assertEquals(STOP_1_STOP_2_FARE, trips.get(0).chargeAmount());
        assertEquals("5500", trips.get(1).pan());
        assertEquals(STOP_2_STOP_3_FARE, trips.get(1).chargeAmount());
    }

    private Tap tap(
            long id,
            String dateTime,
            TapType tapType,
            StopId stopId,
            String busId,
            String pan
    ) {
        return tap(
                id,
                dateTime,
                tapType,
                stopId,
                "Company1",
                busId,
                pan
        );
    }

    private Tap tap(
            long id,
            String dateTime,
            TapType tapType,
            StopId stopId,
            String companyId,
            String busId,
            String pan
    ) {
        return new Tap(
                id,
                dateTime(dateTime),
                tapType,
                stopId,
                companyId,
                busId,
                pan
        );
    }

    private LocalDateTime dateTime(String value) {
        return LocalDateTime.parse(value);
    }
}
