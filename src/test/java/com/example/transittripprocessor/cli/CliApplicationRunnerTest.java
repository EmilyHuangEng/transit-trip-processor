package com.example.transittripprocessor.cli;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliApplicationRunnerTest {

    @Test
    void executesCommandMatchingFirstNonOptionArgument() {
        RecordingCommand generateTrips = new RecordingCommand("generate-trips");
        RecordingCommand anotherCommand = new RecordingCommand("another-command");
        CliApplicationRunner runner = new CliApplicationRunner(
                List.of(generateTrips, anotherCommand)
        );
        ApplicationArguments arguments = new DefaultApplicationArguments(
                "generate-trips",
                "--input=input.csv",
                "--output=output.csv"
        );

        runner.run(arguments);

        assertTrue(generateTrips.wasExecuted());
        assertSame(arguments, generateTrips.receivedArguments());
        assertFalse(anotherCommand.wasExecuted());
    }

    @Test
    void rejectsMissingCommand() {
        CliApplicationRunner runner = new CliApplicationRunner(
                List.of(new RecordingCommand("generate-trips"))
        );
        ApplicationArguments arguments = new DefaultApplicationArguments(
                "--input=input.csv"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> runner.run(arguments)
        );

        assertEquals(
                "Missing command. Available commands: generate-trips",
                exception.getMessage()
        );
    }

    @Test
    void rejectsUnknownCommand() {
        RecordingCommand generateTrips = new RecordingCommand("generate-trips");
        CliApplicationRunner runner = new CliApplicationRunner(
                List.of(generateTrips)
        );
        ApplicationArguments arguments = new DefaultApplicationArguments(
                "unknown-command"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> runner.run(arguments)
        );

        assertEquals(
                "Unknown command: unknown-command. Available commands: generate-trips",
                exception.getMessage()
        );
        assertFalse(generateTrips.wasExecuted());
    }

    private static final class RecordingCommand implements CliCommand {

        private final String name;
        private ApplicationArguments receivedArguments;

        private RecordingCommand(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public void execute(ApplicationArguments arguments) {
            receivedArguments = arguments;
        }

        private boolean wasExecuted() {
            return receivedArguments != null;
        }

        private ApplicationArguments receivedArguments() {
            return receivedArguments;
        }
    }
}
