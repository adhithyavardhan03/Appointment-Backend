package com.anurag.appointment.service;

import com.anurag.appointment.entity.Customer;
import com.anurag.appointment.entity.Service;
import com.anurag.appointment.entity.Waitlist;
import com.anurag.appointment.repository.WaitlistRepository;
import com.anurag.appointment.repository.MatchRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.LocalDate;

@org.springframework.stereotype.Service
public class WaitlistService {
    private final WaitlistRepository waitlistRepository;
    private final CustomerService customerService;
    private final ServiceService serviceService;
    private final MatchRepository matchRepository;
    private final ProviderService providerService;

    public WaitlistService(WaitlistRepository waitlistRepository, CustomerService customerService, ServiceService serviceService, MatchRepository matchRepository, ProviderService providerService) {
        this.waitlistRepository = waitlistRepository;
        this.customerService = customerService;
        this.serviceService = serviceService;
        this.matchRepository = matchRepository;
        this.providerService = providerService;
    }

    public List<Waitlist> getAllWaitlistEntries() {
        return waitlistRepository.findAll();
    }

    public Waitlist getWaitlistById(Long id) {
        return waitlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Waitlist entry not found"));
    }

    @Transactional
    public Waitlist createWaitlistEntry(Waitlist waitlist) {
        Customer customer = waitlist.getCustomer();
        Service service = waitlist.getService();
        if (customer == null || customer.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer is required");
        }
        if (service == null || service.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service is required");
        }
        if (waitlist.getPreferredDate() == null || waitlist.getPreferredDate().isBefore(LocalDate.now())
                || waitlist.getPreferredStartTime() == null
                || waitlist.getPreferredEndTime() == null
                || !waitlist.getPreferredEndTime().isAfter(waitlist.getPreferredStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A preferred date and a valid time range are required");
        }

        waitlist.setCustomer(customerService.getCustomerById(customer.getId()));
        waitlist.setService(serviceService.getServiceById(service.getId()));
        waitlist.setStatus("ACTIVE");
        return waitlistRepository.save(waitlist);
    }

    @Transactional
    public Waitlist updateWaitlistEntry(Long id, Waitlist updatedWaitlist) {
        Waitlist existing = getWaitlistById(id);
        if (!existing.getCustomer().getId().equals(updatedWaitlist.getCustomer().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Waitlist entry not found");
        }
        if (updatedWaitlist.getService() != null && updatedWaitlist.getService().getId() != null) {
            existing.setService(serviceService.getServiceById(updatedWaitlist.getService().getId()));
        }
        if (updatedWaitlist.getPreferredDate() == null || updatedWaitlist.getPreferredDate().isBefore(LocalDate.now())
                || updatedWaitlist.getPreferredStartTime() == null || updatedWaitlist.getPreferredEndTime() == null
                || !updatedWaitlist.getPreferredEndTime().isAfter(updatedWaitlist.getPreferredStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A future preferred date and a valid time range are required");
        }
        existing.setPreferredDate(updatedWaitlist.getPreferredDate());
        existing.setPreferredStartTime(updatedWaitlist.getPreferredStartTime());
        existing.setPreferredEndTime(updatedWaitlist.getPreferredEndTime());
        return waitlistRepository.save(existing);
    }

    @Transactional
    public void deleteWaitlistEntry(Long id, Long customerId) {
        Waitlist waitlist = getWaitlistById(id);
        if (!waitlist.getCustomer().getId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Waitlist entry not found");
        }
        if (matchRepository.existsByWaitlistIdAndStatus(id, "PENDING")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Respond to the pending slot offer before leaving the waitlist");
        }
        waitlist.setStatus("LEFT");
        waitlistRepository.save(waitlist);
    }

    public List<Waitlist> getActiveWaitlistEntriesForService(Long serviceId) {
        return waitlistRepository.findActiveCandidatesForUpdate(serviceId, "ACTIVE");
    }

    public List<Waitlist> getWaitlistEntriesForCustomer(Long customerId) {
        customerService.getCustomerById(customerId);
        return waitlistRepository.findByCustomerId(customerId);
    }

    public List<Waitlist> getWaitlistEntriesForProvider(Long providerId) {
        providerService.getProviderById(providerId);
        return waitlistRepository.findByServiceProviderId(providerId);
    }

    public Waitlist updateCustomerEntry(Long id, Waitlist updatedWaitlist, Long customerId) {
        Waitlist existing = getWaitlistById(id);
        if (!existing.getCustomer().getId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Waitlist entry not found");
        }
        updatedWaitlist.setCustomer(existing.getCustomer());
        return updateWaitlistEntry(id, updatedWaitlist);
    }
}
