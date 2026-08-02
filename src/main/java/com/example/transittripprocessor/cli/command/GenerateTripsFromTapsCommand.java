package com.example.transittripprocessor.cli.command;

import com.example.transittripprocessor.cli.CliCommand;
import com.example.transittripprocessor.service.TapToTripProcessor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

@Component
public class GenerateTripsFromTapsCommand implements CliCommand {

    private static final String COMMAND_NAME =
            "generate-trips";

    private final TapToTripProcessor processor;

    public GenerateTripsFromTapsCommand(TapToTripProcessor processor) {
        this.processor = processor;
    }

    @Override
    public String name() {
        return COMMAND_NAME;
    }

    @Override
    public void execute(ApplicationArguments arguments) {
        String input = getRequiredArgument(arguments, "input");
        String output = getRequiredArgument(arguments, "output");

        try {
            processor.process(Path.of(input), Path.of(output));
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Failed to generate trips from " + input
                            + " to " + output,
                    exception
            );
        }
    }

    private String getRequiredArgument(
            ApplicationArguments arguments,
            String name
    ) {
        var values = arguments.getOptionValues(name);

        if (values == null || values.size() != 1) {
            throw new IllegalArgumentException(
                    "Expected exactly one --" + name
                            + " argument"
            );
        }

        return values.getFirst();
    }
}
