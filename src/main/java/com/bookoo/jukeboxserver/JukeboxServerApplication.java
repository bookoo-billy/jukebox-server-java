package com.bookoo.jukeboxserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JukeboxServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(JukeboxServerApplication.class, args);
	}

}
