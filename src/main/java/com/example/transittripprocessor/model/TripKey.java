package com.example.transittripprocessor.model;

import java.util.Objects;

/**
 * Identifies tap events that can belong to the same trip.
 */
public record TripKey(String pan, String companyId, String busId) {

    public TripKey {
        Objects.requireNonNull(pan, "pan must not be null");
        Objects.requireNonNull(companyId, "companyId must not be null");
        Objects.requireNonNull(busId, "busId must not be null");
    }

    /**
     * Creates the matching key for a tap by extracting its card, company,
     * and bus identifiers. Taps with the same key are eligible to be matched
     * as the ON and OFF events of one trip.
     *
     * @param tap the tap event whose matching identity is required
     * @return a key containing the tap's PAN, company ID, and bus ID
     */
    public static TripKey from(Tap tap) {
        Objects.requireNonNull(tap, "tap must not be null");
        return new TripKey(tap.pan(), tap.companyId(), tap.busId());
    }
}
