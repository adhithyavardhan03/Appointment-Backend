package com.anurag.appointment.controller;

import com.anurag.appointment.entity.Service;
import com.anurag.appointment.service.ServiceService;
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
import com.anurag.appointment.security.JwtAccount;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public List<Service> getAllServices(@org.springframework.web.bind.annotation.RequestParam(required = false) Long providerId) {
        if (providerId != null) {
            return serviceService.getServicesForProvider(providerId);
        }
        return serviceService.getAllServices();
    }

    @GetMapping("/{id}")
    public Service getServiceById(@PathVariable Long id) {
        return serviceService.getServiceById(id);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('PROVIDER')")
    public List<Service> getMyServices(@AuthenticationPrincipal Jwt jwt) {
        return serviceService.getServicesForProvider(JwtAccount.id(jwt));
    }

    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public Service createService(@RequestBody Service service, @AuthenticationPrincipal Jwt jwt) {
        return serviceService.createService(service, JwtAccount.id(jwt));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public Service updateService(@PathVariable Long id, @RequestBody Service service, @AuthenticationPrincipal Jwt jwt) {
        return serviceService.updateService(id, service, JwtAccount.id(jwt));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public void deleteService(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        serviceService.deleteService(id, JwtAccount.id(jwt));
    }
}
