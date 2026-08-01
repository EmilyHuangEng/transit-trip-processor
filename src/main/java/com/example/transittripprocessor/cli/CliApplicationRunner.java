package com.example.transittripprocessor.cli;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CliApplicationRunner implements ApplicationRunner {

    private final Map<String, CliCommand> commands;

    public CliApplicationRunner(List<CliCommand> commands) {
        this.commands = commands.stream().collect(Collectors.toMap(
                CliCommand::name,
                Function.identity()
        ));
    }

    @Override
    public void run(ApplicationArguments arguments) {
        List<String> commandArguments = arguments.getNonOptionArgs();

        if (commandArguments.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing command. Available commands: "
                            + String.join(", ", commands.keySet())
            );
        }

        String commandName = commandArguments.get(0);
        CliCommand command = commands.get(commandName);

        if (command == null) {
            throw new IllegalArgumentException(
                    "Unknown command: " + commandName
                            + ". Available commands: "
                            + String.join(", ", commands.keySet())
            );
        }

        command.execute(arguments);
    }
}