package com.anurag.appointment.service;

import com.anurag.appointment.entity.Provider;
import com.anurag.appointment.entity.Service;
import com.anurag.appointment.entity.Slot;
import com.anurag.appointment.repository.SlotRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.LocalDate;
import org.springframework.data.domain.Sort;

@org.springframework.stereotype.Service
public class SlotService {
    private final SlotRepository slotRepository;
    private final ServiceService serviceService;
    private final ProviderService providerService;

    public SlotService(SlotRepository slotRepository, ServiceService serviceService, ProviderService providerService) {
        this.slotRepository = slotRepository;
        this.serviceService = serviceService;
        this.providerService = providerService;
    }

    public List<Slot> getAllSlots() {
        return slotRepository.findAll();
    }

    public List<Slot> getAvailableSlots() {
        return slotRepository.findByStatus("AVAILABLE");
    }

    public List<Slot> getSlotsForProvider(Long providerId) {
        providerService.getProviderById(providerId);
        return slotRepository.findByProviderId(providerId);
    }

    public Slot getSlotForProvider(Long id, Long providerId) {
        Slot slot = getSlotById(id);
        if (!slot.getProvider().getId().equals(providerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found");
        }
        return slot;
    }

    public Slot save(Slot slot) {
        return slotRepository.save(slot);
    }

    public Slot getSlotById(Long id) {
        return slotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found"));
    }

    public Slot getSlotForUpdate(Long id) {
        return slotRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found"));
    }

    @Transactional
    public Slot createSlot(Slot slot, Long providerId) {
        Service service = slot.getService();
        if (service == null || service.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service is required");
        }
        if (slot.getDate() == null || slot.getDate().isBefore(LocalDate.now())
                || slot.getStartTime() == null || slot.getEndTime() == null
                || !slot.getEndTime().isAfter(slot.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A future date and a valid time range are required");
        }
        Service existingService = serviceService.getServiceById(service.getId());
        if (!existingService.getProvider().getId().equals(providerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot provider must own the selected service");
        }
        slot.setService(existingService);
        slot.setProvider(existingService.getProvider());
        if (slot.getStatus() == null || slot.getStatus().isBlank()) {
            slot.setStatus("AVAILABLE");
        }
        return slotRepository.save(slot);
    }

    @Transactional
    public Slot updateSlot(Long id, Slot updatedSlot, Long providerId) {
        Slot existing = getSlotById(id);
        if (!existing.getProvider().getId().equals(providerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found");
        }
        if ("BOOKED".equals(existing.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booked slots cannot be edited");
        }
        Service targetService = existing.getService();
        if (updatedSlot.getService() != null && updatedSlot.getService().getId() != null) {
            targetService = serviceService.getServiceById(updatedSlot.getService().getId());
        }
        if (targetService.getProvider() == null || !targetService.getProvider().getId().equals(providerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot provider must own the selected service");
        }
        existing.setService(targetService);
        if (updatedSlot.getDate() == null || updatedSlot.getDate().isBefore(LocalDate.now())
                || updatedSlot.getStartTime() == null || updatedSlot.getEndTime() == null
                || !updatedSlot.getEndTime().isAfter(updatedSlot.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A future date and a valid time range are required");
        }
        existing.setDate(updatedSlot.getDate());
        existing.setStartTime(updatedSlot.getStartTime());
        existing.setEndTime(updatedSlot.getEndTime());
        if (updatedSlot.getStatus() != null) {
            existing.setStatus(updatedSlot.getStatus());
        }
        return slotRepository.save(existing);
    }

    @Transactional
    public void deleteSlot(Long id, Long providerId) {
        Slot slot = getSlotForProvider(id, providerId);
        if (!"AVAILABLE".equals(slot.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only available slots can be deleted");
        }
        slotRepository.delete(slot);
    }
}
