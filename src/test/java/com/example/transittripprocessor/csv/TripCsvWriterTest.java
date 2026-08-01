package com.example.transittripprocessor.csv;

import com.example.transittripprocessor.model.StopId;
import com.example.transittripprocessor.model.Trip;
import com.example.transittripprocessor.model.TripStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TripCsvWriterTest {

    @TempDir
    Path tempDirectory;

    private final TripCsvWriter writer = new TripCsvWriter();

    @Test
    void writesTripsUsingTheExpectedFormat() throws IOException {
        Path output = tempDirectory.resolve("trips.csv");
        Trip trip = new Trip(
                LocalDateTime.of(2023, 1, 22, 13, 0),
                LocalDateTime.of(2023, 1, 22, 13, 5),
                300,
                StopId.STOP_1,
                StopId.STOP_2,
                new BigDecimal("3.25"),
                "Company1",
                "Bus37",
                "5500005555555559",
                TripStatus.COMPLETED
        );

        writer.write(output, List.of(trip));

        assertEquals(List.of(
                "Started, Finished, DurationSecs, FromStopId, ToStopId, "
                        + "ChargeAmount, CompanyId, BusID, PAN, Status",
                "22-01-2023 13:00:00, 22-01-2023 13:05:00, 300, "
                        + "Stop1, Stop2, $3.25, Company1, Bus37, "
                        + "5500005555555559, COMPLETED"
        ), Files.readAllLines(output));
    }
}
