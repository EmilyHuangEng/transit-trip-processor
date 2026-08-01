package com.example.transittripprocessor.csv;

import com.example.transittripprocessor.model.StopId;
import com.example.transittripprocessor.model.Tap;
import com.example.transittripprocessor.model.TapType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TapCsvReaderTest {

    @TempDir
    Path tempDirectory;

    private final TapCsvReader reader = new TapCsvReader();

    @Test
    void readsTapRows() throws IOException {
        Path input = tempDirectory.resolve("taps.csv");
        Files.writeString(input, """
                ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN
                1, 22-01-2023 13:00:00, ON, Stop1, Company1, Bus37, 5500005555555559
                2, 22-01-2023 13:05:00, OFF, Stop2, Company1, Bus37, 5500005555555559
                """);

        List<Tap> taps = reader.read(input);

        assertEquals(List.of(
                new Tap(
                        1,
                        LocalDateTime.of(2023, 1, 22, 13, 0),
                        TapType.ON,
                        StopId.STOP_1,
                        "Company1",
                        "Bus37",
                        "5500005555555559"
                ),
                new Tap(
                        2,
                        LocalDateTime.of(2023, 1, 22, 13, 5),
                        TapType.OFF,
                        StopId.STOP_2,
                        "Company1",
                        "Bus37",
                        "5500005555555559"
                )
        ), taps);
    }

    @Test
    void readsLowercaseStopId() throws IOException {
        Path input = tempDirectory.resolve("lowercase-stop-id.csv");
        Files.writeString(input, """
                ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN
                1, 22-01-2023 13:00:00, ON, stop1, Company1, Bus37, 5500005555555559
                """);

        List<Tap> taps = reader.read(input);

        assertEquals(StopId.STOP_1, taps.getFirst().stopId());
    }

    @Test
    void reportsTheLineNumberForAnInvalidRow() throws IOException {
        Path input = tempDirectory.resolve("invalid.csv");
        Files.writeString(input, """
                ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN
                not-a-number, 22-01-2023 13:00:00, ON, Stop1, Company1, Bus37, 1234
                """);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reader.read(input)
        );

        assertTrue(exception.getMessage().contains("line 2"));
    }
}
