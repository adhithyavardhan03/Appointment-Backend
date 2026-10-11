package com.anurag.appointment.service;

import com.anurag.appointment.entity.Provider;
import com.anurag.appointment.entity.Service;
import com.anurag.appointment.repository.ServiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@org.springframework.stereotype.Service
public class ServiceService {
    private final ServiceRepository serviceRepository;
    private final ProviderService providerService;

    public ServiceService(ServiceRepository serviceRepository, ProviderService providerService) {
        this.serviceRepository = serviceRepository;
        this.providerService = providerService;
    }

    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    public List<Service> getServicesForProvider(Long providerId) {
        providerService.getProviderById(providerId);
        return serviceRepository.findByProviderId(providerId);
    }

    public Service getServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
    }

    @Transactional
    public Service createService(Service service, Long providerId) {
        service.setProvider(providerService.getProviderById(providerId));
        return serviceRepository.save(service);
    }

    @Transactional
    public Service updateService(Long id, Service updatedService, Long providerId) {
        Service existing = getServiceById(id);
        if (!existing.getProvider().getId().equals(providerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found");
        }
        existing.setServiceName(updatedService.getServiceName());
        existing.setDescription(updatedService.getDescription());
        existing.setDuration(updatedService.getDuration());
        existing.setPrice(updatedService.getPrice());
        return serviceRepository.save(existing);
    }

    @Transactional
    public void deleteService(Long id, Long providerId) {
        Service service = getServiceById(id);
        if (!service.getProvider().getId().equals(providerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found");
        }
        serviceRepository.delete(service);
    }
}
