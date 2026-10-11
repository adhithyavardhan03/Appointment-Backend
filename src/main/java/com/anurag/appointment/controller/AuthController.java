package com.anurag.appointment.controller;

import com.anurag.appointment.dto.AccountResponse;
import com.anurag.appointment.dto.AuthResponse;
import com.anurag.appointment.dto.LoginRequest;
import com.anurag.appointment.dto.RegisterRequest;
import com.anurag.appointment.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.anurag.appointment.security.JwtAccount;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/{role}")
    public AuthResponse register(@PathVariable String role, @Valid @RequestBody RegisterRequest request) {
        return authService.register(role, request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AccountResponse me(@AuthenticationPrincipal Jwt jwt) {
        return new AccountResponse(
                JwtAccount.id(jwt),
                jwt.getClaimAsString("name"),
                jwt.getSubject(),
                jwt.getClaimAsString("phone"),
                jwt.getClaimAsString("role")
        );
    }
}
