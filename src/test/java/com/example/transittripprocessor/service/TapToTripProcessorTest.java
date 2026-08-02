package com.example.transittripprocessor.service;

import com.example.transittripprocessor.csv.TapCsvReader;
import com.example.transittripprocessor.csv.TripCsvWriter;
import com.example.transittripprocessor.model.Tap;
import com.example.transittripprocessor.model.Trip;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TapToTripProcessorTest {

    @TempDir
    Path tempDirectory;

    @Mock
    TapCsvReader tapCsvReader;

    @Mock
    TapToTripMatcher tapToTripMatcher;

    @Mock
    TripCsvWriter tripCsvWriter;

    @InjectMocks
    TapToTripProcessor processor;

    @Test
    void readsMatchesAndWritesInOrder() throws IOException {
        Path input = tempDirectory.resolve("taps.csv");
        Path output = tempDirectory.resolve("trips.csv");
        List<Tap> taps = List.of();
        List<Trip> trips = List.of();

        when(tapCsvReader.read(input)).thenReturn(taps);
        when(tapToTripMatcher.match(taps)).thenReturn(trips);

        processor.process(input, output);

        InOrder calls = inOrder(
                tapCsvReader,
                tapToTripMatcher,
                tripCsvWriter
        );
        calls.verify(tapCsvReader).read(input);
        calls.verify(tapToTripMatcher).match(taps);
        calls.verify(tripCsvWriter).write(output, trips);
    }
}
