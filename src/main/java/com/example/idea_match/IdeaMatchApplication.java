package com.example.idea_match;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class IdeaMatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdeaMatchApplication.class, args);
	}

}
