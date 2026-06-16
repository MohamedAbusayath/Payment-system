package com.payment.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.payment.dto.LoginRequestDTO;
import com.payment.dto.LoginResponseDTO;
import com.payment.dto.RegisterRequestDTO;
import com.payment.entity.User;
import com.payment.repository.UserRepository;
import com.payment.security.JwtService;

@Service
public class AuthService {

	private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthService(UserRepository repo,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authManager,
                       JwtService jwtService) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }
    
    
	public String register(RegisterRequestDTO reg) {
		User u=new User();
		u.setUsername(reg.getUsername());
		u.setPassword(passwordEncoder.encode(reg.getPassword()));
		u.setRole(reg.getRole());
		repo.save(u);
		return "Registered Successfully";
	}
	
	public LoginResponseDTO login(LoginRequestDTO req) {

	    authManager.authenticate(
	        new UsernamePasswordAuthenticationToken(
	            req.getUsername(),
	            req.getPassword()
	        )
	    );

	    String token =
	            jwtService.generateToken(
	                    req.getUsername());

	    return new LoginResponseDTO(token);
	}
}
