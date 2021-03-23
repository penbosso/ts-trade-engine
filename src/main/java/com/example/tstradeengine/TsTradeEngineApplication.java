package com.example.tstradeengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class TsTradeEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(TsTradeEngineApplication.class, args);
	}
	static int requestNumber = 0;
	@PostMapping("/mdsubscription")
	public void mddata(@RequestBody Object request) {
		requestNumber = requestNumber +1;
		System.out.println("Request ->"+requestNumber+": "+request.toString());

	}

}
