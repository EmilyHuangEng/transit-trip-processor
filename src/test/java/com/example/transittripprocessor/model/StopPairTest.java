package com.example.transittripprocessor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StopPairTest {

    @Test
    void treatsBothTravelDirectionsAsTheSamePair() {
        StopPair outbound = new StopPair(StopId.STOP_1, StopId.STOP_3);
        StopPair inbound = new StopPair(StopId.STOP_3, StopId.STOP_1);

        assertEquals(outbound, inbound);
        assertEquals(outbound.hashCode(), inbound.hashCode());
    }

    @Test
    void reportsWhetherAStopBelongsToThePair() {
        StopPair pair = new StopPair(StopId.STOP_1, StopId.STOP_2);

        assertTrue(pair.includes(StopId.STOP_1));
        assertTrue(pair.includes(StopId.STOP_2));
        assertFalse(pair.includes(StopId.STOP_3));
    }

    @Test
    void rejectsANullStop() {
        assertThrows(
                NullPointerException.class,
                () -> new StopPair(StopId.STOP_1, null)
        );
    }
}
