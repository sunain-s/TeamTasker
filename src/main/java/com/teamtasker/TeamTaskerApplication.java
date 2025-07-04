package com.teamtasker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
public class TeamTaskerApplication {
	public static void main(String[] args) {
		SpringApplication.run(TeamTaskerApplication.class, args);
	}
}
