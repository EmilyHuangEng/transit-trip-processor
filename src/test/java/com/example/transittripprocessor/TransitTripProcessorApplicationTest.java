package com.example.transittripprocessor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(args = {
		"generate-trips",
		"--input=input.csv",
		"--output=output.csv"
})
class TransitTripProcessorApplicationTest {

	@Test
	void contextLoads() {
	}

}
