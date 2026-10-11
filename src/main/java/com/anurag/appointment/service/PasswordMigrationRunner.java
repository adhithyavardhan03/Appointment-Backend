package com.anurag.appointment.service;

import com.anurag.appointment.entity.Customer;
import com.anurag.appointment.entity.Provider;
import com.anurag.appointment.repository.CustomerRepository;
import com.anurag.appointment.repository.ProviderRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PasswordMigrationRunner implements ApplicationRunner {
    private final CustomerRepository customerRepository;
    private final ProviderRepository providerRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationRunner(CustomerRepository customerRepository, ProviderRepository providerRepository,
                                   PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.providerRepository = providerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        customerRepository.findAll().stream()
                .filter(customer -> customer.getPassword() != null && !customer.getPassword().isBlank())
                .filter(customer -> !isBcryptHash(customer.getPassword()))
                .forEach(this::hashLegacyPassword);
        providerRepository.findAll().stream()
                .filter(provider -> provider.getPassword() != null && !provider.getPassword().isBlank())
                .filter(provider -> !isBcryptHash(provider.getPassword()))
                .forEach(this::hashLegacyPassword);
    }

    private boolean isBcryptHash(String password) {
        return password != null && password.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    }

    private void hashLegacyPassword(Customer customer) {
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customerRepository.save(customer);
    }

    private void hashLegacyPassword(Provider provider) {
        provider.setPassword(passwordEncoder.encode(provider.getPassword()));
        providerRepository.save(provider);
    }
}
