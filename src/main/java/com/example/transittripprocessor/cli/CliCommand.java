package com.example.transittripprocessor.cli;

import org.springframework.boot.ApplicationArguments;

public interface CliCommand {

    String name();

    void execute(ApplicationArguments arguments);
}