package com.example.transittripprocessor.service;

import com.example.transittripprocessor.model.Tap;
import com.example.transittripprocessor.model.Trip;
import com.example.transittripprocessor.model.TripKey;
import com.example.transittripprocessor.model.TripStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.transittripprocessor.model.TapType.ON;

/**
 * Matches tap events into passenger trips.
 *
 * <p>Taps are processed in chronological order. An ON tap is kept until an
 * OFF tap with the same PAN, company ID, and bus ID arrives. A matched pair
 * becomes a completed trip, or a cancelled trip when both taps occurred at
 * the same stop. An ON tap that is never matched becomes an incomplete trip,
 * while an OFF tap without a matching ON is ignored.</p>
 *
 * <p>This service decides how taps are paired and which trip status applies.
 * Fare amounts are delegated to {@link FareCalculator}.</p>
 */
@Service
public class TapToTripMatcher {

    /**
     * Sort tap events by DateTimeUTC, using ID as a tie-breaker.
     * This ensures chronological and deterministic processing.
     */
    private static final Comparator<Tap> TAP_ORDER = Comparator
            .comparing(Tap::dateTimeUtc)
            .thenComparingLong(Tap::id);

    /**
     * Sort trips by start time, with identity fields as deterministic
     * tie-breakers.
     */
    private static final Comparator<Trip> TRIP_ORDER = Comparator
            .comparing(Trip::started)
            .thenComparing(Trip::pan)
            .thenComparing(Trip::companyId)
            .thenComparing(Trip::busId);

    private final FareCalculator fareCalculator;

    public TapToTripMatcher(FareCalculator fareCalculator) {
        this.fareCalculator = fareCalculator;
    }

    public List<Trip> match(List<Tap> taps) {
        // report match(null) error with better error message.
        Objects.requireNonNull(taps, "taps must not be null");

        List<Tap> orderedTaps = taps.stream()
                .map(tap -> Objects.requireNonNull(
                        tap,
                        "taps must not contain null"
                ))
                .sorted(TAP_ORDER)
                .toList();

        Map<TripKey, Tap> activeTapOns = new HashMap<>();
        List<Trip> trips = new ArrayList<>();

        for (Tap tap : orderedTaps) {
            TripKey tripKey = TripKey.from(tap);

            switch (tap.tapType()) {
                case ON -> {
                    Tap previousTapOn = activeTapOns.get(tripKey);

                    if (previousTapOn != null) {
                        // When has previousTapOn, means there is no matching Tap off,
                        // so consider as incomplete trip.
                        trips.add(createIncompleteTrip(previousTapOn));
                    }

                    activeTapOns.put(tripKey, tap);
                }

                case OFF -> {
                    Tap matchingTapOn = activeTapOns.get(tripKey);

                    if (matchingTapOn != null) {
                        if (matchingTapOn.stopId() == tap.stopId()) {
                            trips.add(createCancelledTrip(matchingTapOn, tap));
                        } else {
                            trips.add(createCompletedTrip(matchingTapOn, tap));
                        }
                    }

                    activeTapOns.remove(tripKey);
                }

                default -> throw new IllegalArgumentException(
                        "Unsupported tap type: " + tap.tapType()
                );
            }
        }

        // Process the tap on that only appear once.
        activeTapOns.values().stream()
                .map(this::createIncompleteTrip)
                .forEach(trips::add);

        return trips.stream()
                .sorted(TRIP_ORDER)
                .toList();
    }

    private Trip createCompletedTrip(Tap tapOn, Tap tapOff) {
        BigDecimal charge = fareCalculator.fareBetween(
                tapOn.stopId(),
                tapOff.stopId()
        );

        return createMatchedTrip(
                tapOn,
                tapOff,
                charge,
                TripStatus.COMPLETED
        );
    }

    private Trip createCancelledTrip(Tap tapOn, Tap tapOff) {
        return createMatchedTrip(
                tapOn,
                tapOff,
                BigDecimal.ZERO,
                TripStatus.CANCELLED
        );
    }

    private Trip createMatchedTrip(
            Tap tapOn,
            Tap tapOff,
            BigDecimal charge,
            TripStatus status
    ) {
        return new Trip(
                tapOn.dateTimeUtc(),
                tapOff.dateTimeUtc(),
                Duration.between(
                        tapOn.dateTimeUtc(),
                        tapOff.dateTimeUtc()
                ).toSeconds(),
                tapOn.stopId(),
                tapOff.stopId(),
                charge,
                tapOn.companyId(),
                tapOn.busId(),
                tapOn.pan(),
                status
        );
    }

    private Trip createIncompleteTrip(Tap tapOn) {
        return new Trip(
                tapOn.dateTimeUtc(),
                null,
                0,
                tapOn.stopId(),
                null,
                fareCalculator.maximumFareFrom(tapOn.stopId()),
                tapOn.companyId(),
                tapOn.busId(),
                tapOn.pan(),
                TripStatus.INCOMPLETE
        );
    }
}
