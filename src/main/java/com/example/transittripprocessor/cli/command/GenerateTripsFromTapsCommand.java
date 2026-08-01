package com.example.transittripprocessor.cli.command;

import com.example.transittripprocessor.cli.CliCommand;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Component
public class GenerateTripsFromTapsCommand implements CliCommand {

    private static final String COMMAND_NAME =
            "generate-trips";

    @Override
    public String name() {
        return COMMAND_NAME;
    }

    @Override
    public void execute(ApplicationArguments arguments) {
        System.out.println(
                ">> in GenerateTripsFromTapsCommand..."
        );

        String input = getRequiredArgument(arguments, "input");
        String output = getRequiredArgument(arguments, "output");

        System.out.println("Input: " + input);
        System.out.println("Output: " + output);
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

        return values.get(0);
    }
}
