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

    private static final Comparator<Tap> TAP_ORDER = Comparator
            .comparing(Tap::dateTimeUtc)
            .thenComparingLong(Tap::id);

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
            TripKey key = TripKey.from(tap);

            if (tap.tapType() == ON) {
                Tap previousTapOn = activeTapOns.put(key, tap);
                if (previousTapOn != null) {
                    trips.add(createIncompleteTrip(previousTapOn));
                }
                continue;
            }

            Tap matchingTapOn = activeTapOns.remove(key);
            if (matchingTapOn != null) {
                trips.add(createClosedTrip(matchingTapOn, tap));
            }
        }

        activeTapOns.values().stream()
                .map(this::createIncompleteTrip)
                .forEach(trips::add);

        return trips.stream()
                .sorted(TRIP_ORDER)
                .toList();
    }

    private Trip createClosedTrip(Tap tapOn, Tap tapOff) {
        boolean cancelled = tapOn.stopId() == tapOff.stopId();
        BigDecimal charge = cancelled
                ? BigDecimal.ZERO
                : fareCalculator.fareBetween(
                        tapOn.stopId(),
                        tapOff.stopId()
                );

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
                cancelled ? TripStatus.CANCELLED : TripStatus.COMPLETED
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
