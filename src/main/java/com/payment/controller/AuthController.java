package com.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.payment.dto.LoginRequestDTO;
import com.payment.dto.LoginResponseDTO;
import com.payment.dto.RegisterRequestDTO;
import com.payment.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody  RegisterRequestDTO req) {
        return ResponseEntity.ok(service.register(req));
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO req){
    	return ResponseEntity.ok(service.login(req));
    }
}