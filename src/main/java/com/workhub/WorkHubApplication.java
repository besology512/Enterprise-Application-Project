package com.workhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WorkHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkHubApplication.class, args);
	}

}
