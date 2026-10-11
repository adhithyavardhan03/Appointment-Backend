package com.anurag.appointment.service;

import com.anurag.appointment.entity.Provider;
import com.anurag.appointment.repository.ProviderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@org.springframework.stereotype.Service
public class ProviderService {
    private final ProviderRepository providerRepository;

    public ProviderService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    public List<Provider> getAllProviders() {
        return providerRepository.findAll();
    }

    public Provider getProviderById(Long id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));
    }

    @Transactional
    public Provider createProvider(Provider provider) {
        return providerRepository.save(provider);
    }

    @Transactional
    public Provider updateProvider(Long id, Provider updatedProvider) {
        Provider existing = getProviderById(id);
        existing.setName(updatedProvider.getName());
        existing.setEmail(updatedProvider.getEmail());
        if (updatedProvider.getPassword() != null && !updatedProvider.getPassword().isBlank()) {
            existing.setPassword(updatedProvider.getPassword());
        }
        existing.setPhone(updatedProvider.getPhone());
        return providerRepository.save(existing);
    }

    @Transactional
    public void deleteProvider(Long id) {
        Provider provider = getProviderById(id);
        providerRepository.delete(provider);
    }
}
