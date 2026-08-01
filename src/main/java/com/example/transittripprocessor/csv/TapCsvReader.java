package com.example.transittripprocessor.csv;

import com.example.transittripprocessor.model.StopId;
import com.example.transittripprocessor.model.Tap;
import com.example.transittripprocessor.model.TapType;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class TapCsvReader {

    static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu HH:mm:ss");

    private static final List<String> EXPECTED_HEADER = List.of(
            "ID",
            "DateTimeUTC",
            "TapType",
            "StopId",
            "CompanyId",
            "BusID",
            "PAN"
    );

    public List<Tap> read(Path inputPath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(
                inputPath,
                StandardCharsets.UTF_8
        )) {
            String header = reader.readLine();
            if (header == null) {
                throw new IllegalArgumentException("Tap CSV is empty: " + inputPath);
            }

            List<String> actualHeader = parseCsvLine(removeBom(header));
            if (!EXPECTED_HEADER.equals(actualHeader)) {
                throw new IllegalArgumentException(
                        "Unexpected tap CSV header. Expected " + EXPECTED_HEADER
                                + " but was " + actualHeader
                );
            }

            List<Tap> taps = new ArrayList<>();
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                taps.add(toTap(parseCsvLine(line), lineNumber));
            }

            // return immutable taps, preventing add()、remove() or clear() from happening.
            return List.copyOf(taps);
        }
    }

    private Tap toTap(List<String> values, int lineNumber) {
        if (values.size() != EXPECTED_HEADER.size()) {
            throw invalidRow(
                    lineNumber,
                    "expected " + EXPECTED_HEADER.size()
                            + " columns but found " + values.size(),
                    null
            );
        }

        try {
            return new Tap(
                    Long.parseLong(values.get(0)),
                    LocalDateTime.parse(values.get(1), DATE_TIME_FORMATTER),
                    TapType.valueOf(values.get(2).toUpperCase(Locale.ROOT)),
                    new StopId(values.get(3)),
                    values.get(4),
                    values.get(5),
                    values.get(6)
            );
        } catch (IllegalArgumentException exception) {
            throw invalidRow(lineNumber, exception.getMessage(), exception);
        }
    }

    private IllegalArgumentException invalidRow(
            int lineNumber,
            String detail,
            Exception cause
    ) {
        return new IllegalArgumentException(
                "Invalid tap CSV row at line " + lineNumber + ": " + detail,
                cause
        );
    }

    static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length()
                        && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }

        if (quoted) {
            throw new IllegalArgumentException("Unclosed quoted CSV value");
        }
        values.add(current.toString().trim());
        return values;
    }

    private String removeBom(String value) {
        return value.startsWith("\uFEFF") ? value.substring(1) : value;
    }
}
