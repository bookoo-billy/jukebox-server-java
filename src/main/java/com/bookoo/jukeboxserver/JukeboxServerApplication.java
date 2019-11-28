package com.bookoo.jukeboxserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javafx.application.Platform;

@SpringBootApplication
@EnableScheduling
public class JukeboxServerApplication {

	public static void main(String[] args) {
		Platform.startup(() -> {
			//NOOP
		});
		SpringApplication.run(JukeboxServerApplication.class, args);
	}
}
