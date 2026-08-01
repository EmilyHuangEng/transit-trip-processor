package com.example.transittripprocessor.csv;

import com.example.transittripprocessor.model.Trip;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
public class TripCsvWriter {

    private static final List<String> HEADER = List.of(
            "Started",
            "Finished",
            "DurationSecs",
            "FromStopId",
            "ToStopId",
            "ChargeAmount",
            "CompanyId",
            "BusID",
            "PAN",
            "Status"
    );

    public void write(Path outputPath, List<Trip> trips) throws IOException {
        Objects.requireNonNull(outputPath, "outputPath must not be null");
        Objects.requireNonNull(trips, "trips must not be null");

        try (BufferedWriter writer = Files.newBufferedWriter(
                outputPath,
                StandardCharsets.UTF_8
        )) {
            writeRow(writer, HEADER);
            for (Trip trip : trips) {
                writeRow(writer, toColumns(Objects.requireNonNull(
                        trip,
                        "trips must not contain null"
                )));
            }
        }
    }

    private List<String> toColumns(Trip trip) {
        return List.of(
                formatDateTime(trip.started()),
                formatDateTime(trip.finished()),
                Long.toString(trip.durationSecs()),
                trip.fromStopId().csvValue(),
                trip.toStopId() == null ? "" : trip.toStopId().csvValue(),
                "$" + trip.chargeAmount().setScale(2, RoundingMode.HALF_UP)
                        .toPlainString(),
                trip.companyId(),
                trip.busId(),
                trip.pan(),
                trip.status().name()
        );
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null
                ? ""
                : TapCsvReader.DATE_TIME_FORMATTER.format(value);
    }

    private void writeRow(BufferedWriter writer, List<String> values)
            throws IOException {
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                writer.write(", ");
            }
            writer.write(escape(values.get(index)));
        }
        writer.newLine();
    }

    private String escape(String value) {
        if (value.indexOf(',') < 0
                && value.indexOf('"') < 0
                && value.indexOf('\n') < 0
                && value.indexOf('\r') < 0) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
