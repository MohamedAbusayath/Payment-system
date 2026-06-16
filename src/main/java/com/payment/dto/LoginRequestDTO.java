package com.payment.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {

	@NotEmpty(message = "Username is required")
	private String username;
	@NotEmpty(message = "Username is required")
	private String password;
}
