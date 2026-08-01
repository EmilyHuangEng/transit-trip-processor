package com.example.transittripprocessor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateTripsIntegrationTest {

    @TempDir
    Path tempDirectory;

    @Test
    void generatesTripsFromTapEvents() throws IOException {
        Path input = tempDirectory.resolve("input.csv");
        Path actualOutput = tempDirectory.resolve("output.csv");

        copyResourceToFile("/input.csv", input);

        try (ConfigurableApplicationContext ignored =
                     new SpringApplicationBuilder(
                             TransitTripProcessorApplication.class
                     )
                             .web(WebApplicationType.NONE)
                             .run(
                                     "generate-trips",
                                     "--input=" + input,
                                     "--output=" + actualOutput
                             )) {
            // ApplicationRunner executes while the application context starts.
        }

        assertTrue(
                Files.exists(actualOutput),
                "The command should create the output CSV file"
        );

        List<String> expectedLines = readResourceLines("/output.csv");
        List<String> actualLines = Files.readAllLines(
                actualOutput,
                StandardCharsets.UTF_8
        );

        assertEquals(expectedLines, actualLines);
    }

    private void copyResourceToFile(
            String resourceName,
            Path destination
    ) throws IOException {
        try (InputStream input = Objects.requireNonNull(
                getClass().getResourceAsStream(resourceName),
                "Missing test resource: " + resourceName
        )) {
            Files.copy(input, destination);
        }
    }

    private List<String> readResourceLines(
            String resourceName
    ) throws IOException {
        try (InputStream input = Objects.requireNonNull(
                getClass().getResourceAsStream(resourceName),
                "Missing test resource: " + resourceName
        )) {
            return new String(
                    input.readAllBytes(),
                    StandardCharsets.UTF_8
            ).lines().toList();
        }
    }
}
