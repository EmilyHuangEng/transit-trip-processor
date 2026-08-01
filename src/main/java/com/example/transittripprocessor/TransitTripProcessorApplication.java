package com.example.transittripprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransitTripProcessorApplication {
	public static void main(String[] args) {
        SpringApplication.run(TransitTripProcessorApplication.class, args);
        System.out.println(">> in TransitTripProcessorApplication main ... ");
    }
}
