package com.payment.controller;

import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.payment.dto.LoginRequestDTO;
import com.payment.dto.LoginResponseDTO;
import com.payment.dto.RegisterRequestDTO;
import com.payment.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(
        name = "Authentication",
        description = "APIs for user registration and login"
)
public class AuthController {

    private final AuthService service;
    private final ParameterNamesModule parameterNamesModule;

    public AuthController(AuthService service, ParameterNamesModule parameterNamesModule) {
        this.service = service;
        this.parameterNamesModule = parameterNamesModule;
    }

    @Operation(summary = "User Register",
            description = "Username, Password, and Role(CHECKER,MAKER,ADMIN)"
    )
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody  RegisterRequestDTO req) {
        return ResponseEntity.ok(service.register(req));
    }


    @Operation(summary = "User Login", description = "User name and Password ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO req){
    	return ResponseEntity.ok(service.login(req));
    }
}