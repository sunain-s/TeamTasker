package com.teamtasker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF to allow H2 console POST requests
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())) // Allow iframe for H2 console
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll() // Allow all access to H2 console
                        .anyRequest().authenticated() // Secure other requests if you want
                )
                .formLogin(form -> form.disable()); // Disable default login form, or customize as needed

        return http.build();
    }
}
