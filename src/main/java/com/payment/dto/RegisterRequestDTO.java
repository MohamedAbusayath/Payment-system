package com.payment.dto;


import jakarta.validation.constraints.NotEmpty;
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
	    private String password;

	    @NotEmpty(message = "Role is required")
	    private String role;
}
