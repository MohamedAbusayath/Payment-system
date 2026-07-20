package com.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.payment.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtFilter) {

		this.jwtFilter = jwtFilter;
	}

	@Bean
	public SecurityFilterChain security(HttpSecurity http) throws Exception {

		return http.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(req -> req
						.requestMatchers("/auth/register", "/auth/login", "/api/payment/history","/sec/encode","/sec/decode","/actuator/**").permitAll()
						.requestMatchers("/api/payment", "/api/payment/my").hasRole("MAKER")
						.requestMatchers("/api/payment/pending", "/api/payment/approve/**", "/api/payment/reject/**",
								"/api/payment/delete/**")
						.hasRole("CHECKER").requestMatchers("/api/payment/report").hasRole("ADMIN")
						.requestMatchers("/api/payment/get").hasAnyRole("CHECKER", "ADMIN").anyRequest()

						.authenticated())
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
