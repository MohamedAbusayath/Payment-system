package com.payment.dto;

import com.payment.enums.UserRole;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

	@NotEmpty(message = "Username is required")
	private String username;

	@NotEmpty(message = "Password is required")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\\\d)(?=.*[@$!%*?&])[A-Za-z\\\\d@$!%*?&]{8,}$", message = "password must contain 8 char."
			+ "\n" + "must contain one or more small/capital/digital/special char.")
	private String password;
	
	@Enumerated(EnumType.STRING)
	private UserRole role;
}