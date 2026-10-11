package com.anurag.appointment.service;

import com.anurag.appointment.entity.Appointment;
import com.anurag.appointment.entity.Customer;
import com.anurag.appointment.entity.Slot;
import com.anurag.appointment.repository.AppointmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@org.springframework.stereotype.Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final CustomerService customerService;
    private final ProviderService providerService;
    private final SlotService slotService;
    private final MatchService matchService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            CustomerService customerService,
            ProviderService providerService,
            SlotService slotService,
            MatchService matchService) {
        this.appointmentRepository = appointmentRepository;
        this.customerService = customerService;
        this.providerService = providerService;
        this.slotService = slotService;
        this.matchService = matchService;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    private Appointment getAppointmentForUpdate(Long id) {
        return appointmentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    @Transactional
    public Appointment createAppointment(Appointment appointment, Long customerId) {
        Slot slot = appointment.getSlot();

        if (slot == null || slot.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot is required");
        }

        Customer existingCustomer = customerService.getCustomerById(customerId);
        Slot existingSlot = slotService.getSlotForUpdate(slot.getId());
        if (!"AVAILABLE".equals(existingSlot.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot is not available");
        }

        existingSlot.setStatus("BOOKED");
        appointment.setCustomer(existingCustomer);
        appointment.setService(existingSlot.getService());
        appointment.setProvider(existingSlot.getProvider());
        appointment.setSlot(existingSlot);
        appointment.setAppointmentDate(existingSlot.getDate());
        appointment.setStartTime(existingSlot.getStartTime());
        appointment.setEndTime(existingSlot.getEndTime());
        appointment.setStatus("BOOKED");
        slotService.save(existingSlot);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment updateAppointment(Long id, Appointment updatedAppointment, Long customerId) {
        Appointment existing = getAppointmentForUpdate(id);
        if (!existing.getCustomer().getId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
        }
        if (!"BOOKED".equals(existing.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only booked appointments can be updated");
        }
        Slot releasedSlot = null;
        if (updatedAppointment.getSlot() != null && updatedAppointment.getSlot().getId() != null
                && (existing.getSlot() == null || !updatedAppointment.getSlot().getId().equals(existing.getSlot().getId()))) {
            Slot replacement = slotService.getSlotForUpdate(updatedAppointment.getSlot().getId());
            if (!"AVAILABLE".equals(replacement.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Replacement slot is not available");
            }
            if (existing.getSlot() != null) {
                existing.getSlot().setStatus("AVAILABLE");
                slotService.save(existing.getSlot());
                releasedSlot = existing.getSlot();
            }
            replacement.setStatus("BOOKED");
            slotService.save(replacement);
            existing.setSlot(replacement);
            existing.setService(replacement.getService());
            existing.setProvider(replacement.getProvider());
            existing.setAppointmentDate(replacement.getDate());
            existing.setStartTime(replacement.getStartTime());
            existing.setEndTime(replacement.getEndTime());
        }
        Appointment saved = appointmentRepository.save(existing);
        if (releasedSlot != null) {
            matchService.createBestMatchForCancelledSlot(releasedSlot);
        }
        return saved;
    }

    @Transactional
    public void deleteAppointment(Long id, Long customerId) {
        Appointment appointment = getAppointmentById(id);
        if (!appointment.getCustomer().getId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
        }
        if ("BOOKED".equals(appointment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cancel a booked appointment before deleting it");
        }
        appointmentRepository.delete(appointment);
    }

    @Transactional
    public com.anurag.appointment.entity.Match cancelAppointment(Long appointmentId, Long accountId, String role) {
        Appointment appointment = getAppointmentForUpdate(appointmentId);
        boolean owner = "customer".equals(role)
                ? appointment.getCustomer().getId().equals(accountId)
                : "provider".equals(role) && appointment.getProvider().getId().equals(accountId);
        if (!owner) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
        }
        if (!"BOOKED".equals(appointment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only booked appointments can be cancelled");
        }

        appointment.setStatus("CANCELLED");
        Slot slot = appointment.getSlot();
        if (slot != null) {
            if (!"BOOKED".equals(slot.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Appointment slot is not booked");
            }
            slot.setStatus("AVAILABLE");
            slotService.save(slot);
        }
        appointmentRepository.save(appointment);

        return matchService.createBestMatchForCancelledSlot(slot);
    }

    public List<Appointment> getAppointmentsForCustomer(Long customerId) {
        customerService.getCustomerById(customerId);
        return appointmentRepository.findByCustomerId(customerId);
    }

    public List<Appointment> getAppointmentsForProvider(Long providerId) {
        providerService.getProviderById(providerId);
        return appointmentRepository.findByProviderId(providerId);
    }

    public Appointment getAppointmentForCustomer(Long appointmentId, Long customerId) {
        Appointment appointment = getAppointmentById(appointmentId);
        if (!appointment.getCustomer().getId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
        }
        return appointment;
    }
}
