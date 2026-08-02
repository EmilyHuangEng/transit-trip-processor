package com.example.transittripprocessor.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TripKeyTest {

    @Test
    void createsAKeyFromTheTapIdentityFields() {
        Tap tap = new Tap(
                1,
                LocalDateTime.of(2023, 1, 22, 13, 0),
                TapType.ON,
                StopId.STOP_1,
                "Company1",
                "Bus37",
                "5500005555555559"
        );

        assertEquals(
                new TripKey(
                        "5500005555555559",
                        "Company1",
                        "Bus37"
                ),
                TripKey.from(tap)
        );
    }

    @Test
    void treatsTheSameCardOnDifferentBusesAsDifferentKeys() {
        TripKey bus36 = new TripKey("1234", "Company1", "Bus36");
        TripKey bus37 = new TripKey("1234", "Company1", "Bus37");

        assertNotEquals(bus36, bus37);
    }
}
