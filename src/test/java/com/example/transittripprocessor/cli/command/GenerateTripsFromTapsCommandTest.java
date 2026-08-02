package com.example.transittripprocessor.cli.command;

import com.example.transittripprocessor.service.TapToTripProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class GenerateTripsFromTapsCommandTest {

    @Mock
    TapToTripProcessor processor;

    @InjectMocks
    GenerateTripsFromTapsCommand command;

    @Test
    void hasTheGenerateTripsCommandName() {
        assertEquals("generate-trips", command.name());
    }

    @Test
    void convertsArgumentsToPathsAndDelegatesToTheProcessor()
            throws IOException {
        var arguments = new DefaultApplicationArguments(
                "generate-trips",
                "--input=input.csv",
                "--output=output.csv"
        );

        command.execute(arguments);

        verify(processor).process(
                Path.of("input.csv"),
                Path.of("output.csv")
        );
    }

    @Test
    void rejectsAMissingInputArgument() {
        var arguments = new DefaultApplicationArguments(
                "generate-trips",
                "--output=output.csv"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.execute(arguments)
        );

        assertEquals(
                "Expected exactly one --input argument",
                exception.getMessage()
        );
        verifyNoInteractions(processor);
    }

    @Test
    void rejectsAMissingOutputArgument() {
        var arguments = new DefaultApplicationArguments(
                "generate-trips",
                "--input=input.csv"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.execute(arguments)
        );

        assertEquals(
                "Expected exactly one --output argument",
                exception.getMessage()
        );
        verifyNoInteractions(processor);
    }

    @Test
    void rejectsDuplicateInputArguments() {
        var arguments = new DefaultApplicationArguments(
                "generate-trips",
                "--input=first.csv",
                "--input=second.csv",
                "--output=output.csv"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.execute(arguments)
        );

        assertEquals(
                "Expected exactly one --input argument",
                exception.getMessage()
        );
        verifyNoInteractions(processor);
    }

    @Test
    void rejectsDuplicateOutputArguments() {
        var arguments = new DefaultApplicationArguments(
                "generate-trips",
                "--input=input.csv",
                "--output=first.csv",
                "--output=second.csv"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.execute(arguments)
        );

        assertEquals(
                "Expected exactly one --output argument",
                exception.getMessage()
        );
        verifyNoInteractions(processor);
    }

    @Test
    void wrapsAnIOExceptionFromTheProcessor() throws IOException {
        var arguments = new DefaultApplicationArguments(
                "generate-trips",
                "--input=input.csv",
                "--output=output.csv"
        );
        IOException cause = new IOException("Cannot read input");
        doThrow(cause).when(processor).process(
                Path.of("input.csv"),
                Path.of("output.csv")
        );

        UncheckedIOException exception = assertThrows(
                UncheckedIOException.class,
                () -> command.execute(arguments)
        );

        assertEquals(
                "Failed to generate trips from input.csv to output.csv",
                exception.getMessage()
        );
        assertSame(cause, exception.getCause());
    }
}
