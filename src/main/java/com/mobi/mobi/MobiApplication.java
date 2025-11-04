package com.mobi.mobi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MobiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MobiApplication.class, args);
	}
}
