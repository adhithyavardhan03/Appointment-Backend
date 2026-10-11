package com.anurag.appointment.controller;

import com.anurag.appointment.entity.Provider;
import com.anurag.appointment.service.ProviderService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import com.anurag.appointment.security.JwtAccount;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {
    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PROVIDER')")
    public Provider getMyProviderProfile(@AuthenticationPrincipal Jwt jwt) {
        return providerService.getProviderById(JwtAccount.id(jwt));
    }
}
