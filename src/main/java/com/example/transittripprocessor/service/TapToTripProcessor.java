package com.example.transittripprocessor.service;

import com.example.transittripprocessor.csv.TapCsvReader;
import com.example.transittripprocessor.csv.TripCsvWriter;
import com.example.transittripprocessor.model.Tap;
import com.example.transittripprocessor.model.Trip;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Service
public class TapToTripProcessor {

    private final TapCsvReader tapCsvReader;
    private final TapToTripMatcher tapToTripMatcher;
    private final TripCsvWriter tripCsvWriter;

    public TapToTripProcessor(
            TapCsvReader tapCsvReader,
            TapToTripMatcher tapToTripMatcher,
            TripCsvWriter tripCsvWriter
    ) {
        this.tapCsvReader = tapCsvReader;
        this.tapToTripMatcher = tapToTripMatcher;
        this.tripCsvWriter = tripCsvWriter;
    }

    public void process(Path inputPath, Path outputPath) throws IOException {
        Objects.requireNonNull(inputPath, "inputPath must not be null");
        Objects.requireNonNull(outputPath, "outputPath must not be null");

        List<Tap> taps = tapCsvReader.read(inputPath);
        List<Trip> trips = tapToTripMatcher.match(taps);
        tripCsvWriter.write(outputPath, trips);
    }
}
