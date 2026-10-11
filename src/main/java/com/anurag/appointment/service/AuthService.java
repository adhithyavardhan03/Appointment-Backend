package com.anurag.appointment.service;

import com.anurag.appointment.dto.AccountResponse;
import com.anurag.appointment.dto.AuthResponse;
import com.anurag.appointment.dto.LoginRequest;
import com.anurag.appointment.dto.RegisterRequest;
import com.anurag.appointment.entity.Customer;
import com.anurag.appointment.entity.Provider;
import com.anurag.appointment.repository.CustomerRepository;
import com.anurag.appointment.repository.ProviderRepository;
import com.anurag.appointment.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class AuthService {
    private final CustomerRepository customerRepository;
    private final ProviderRepository providerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(CustomerRepository customerRepository, ProviderRepository providerRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.customerRepository = customerRepository;
        this.providerRepository = providerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(String role, RegisterRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes");
        }
        String email = normalizeEmail(request.email());
        if (customerRepository.existsByEmailIgnoreCase(email) || providerRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        String encodedPassword = passwordEncoder.encode(request.password());
        AccountResponse account;
        if ("customer".equals(role)) {
            Customer customer = new Customer();
            customer.setName(request.name().trim());
            customer.setEmail(email);
            customer.setPassword(encodedPassword);
            customer.setPhone(request.phone());
            customer = customerRepository.save(customer);
            account = new AccountResponse(customer.getId(), customer.getName(), customer.getEmail(), customer.getPhone(), role);
        } else if ("provider".equals(role)) {
            Provider provider = new Provider();
            provider.setName(request.name().trim());
            provider.setEmail(email);
            provider.setPassword(encodedPassword);
            provider.setPhone(request.phone());
            provider = providerRepository.save(provider);
            account = new AccountResponse(provider.getId(), provider.getName(), provider.getEmail(), provider.getPhone(), role);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported account role");
        }
        return issue(account);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        Customer customer = customerRepository.findByEmailIgnoreCase(email).orElse(null);
        if (customer != null && passwordEncoder.matches(request.password(), customer.getPassword())) {
            return issue(new AccountResponse(customer.getId(), customer.getName(), customer.getEmail(), customer.getPhone(), "customer"));
        }
        Provider provider = providerRepository.findByEmailIgnoreCase(email).orElse(null);
        if (provider != null && passwordEncoder.matches(request.password(), provider.getPassword())) {
            return issue(new AccountResponse(provider.getId(), provider.getName(), provider.getEmail(), provider.getPhone(), "provider"));
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    private AuthResponse issue(AccountResponse account) {
        return new AuthResponse(jwtService.createToken(account), "Bearer", jwtService.getTokenLifetimeSeconds(), account);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
