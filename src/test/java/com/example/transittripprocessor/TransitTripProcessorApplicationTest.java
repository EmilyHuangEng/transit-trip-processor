package com.example.transittripprocessor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.transittripprocessor.service.TapToTripProcessor;

@SpringBootTest(args = {
		"generate-trips",
		"--input=input.csv",
		"--output=output.csv"
})
class TransitTripProcessorApplicationTest {

	@MockitoBean
	TapToTripProcessor processor;

	@Test
	void contextLoads() {
	}
}
