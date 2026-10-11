package com.anurag.appointment;

import com.anurag.appointment.entity.Appointment;
import com.anurag.appointment.entity.Customer;
import com.anurag.appointment.entity.Match;
import com.anurag.appointment.entity.Provider;
import com.anurag.appointment.entity.Slot;
import com.anurag.appointment.entity.Waitlist;
import com.anurag.appointment.repository.AppointmentRepository;
import com.anurag.appointment.repository.MatchRepository;
import com.anurag.appointment.repository.SlotRepository;
import com.anurag.appointment.repository.WaitlistRepository;
import com.anurag.appointment.service.AppointmentService;
import com.anurag.appointment.service.MatchService;
import com.anurag.appointment.service.SlotService;
import com.anurag.appointment.service.WaitlistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(properties = "app.jwt.secret=test-only-secret-key-with-32-bytes-minimum")
@AutoConfigureMockMvc
@Transactional
class AppointmentWorkflowIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private com.anurag.appointment.repository.ProviderRepository providerRepository;
    @Autowired
    private com.anurag.appointment.repository.CustomerRepository customerRepository;
    @Autowired
    private com.anurag.appointment.repository.ServiceRepository serviceRepository;
    @Autowired
    private SlotRepository slotRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private WaitlistRepository waitlistRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private WaitlistService waitlistService;
    @Autowired
    private MatchService matchService;
    @Autowired
    private SlotService slotService;
    @Test
    void cancellationOffersBestMatchAndRejectionAdvancesToNextCustomer() {
        String suffix = UUID.randomUUID().toString();
        Provider provider = new Provider();
        provider.setName("Workflow test provider");
        provider.setEmail("provider-" + suffix + "@example.test");
        provider.setPassword("test-only");
        provider = providerRepository.save(provider);

        Customer cancellingCustomer = createCustomer("cancelling-" + suffix);
        Customer firstWaitlistedCustomer = createCustomer("first-" + suffix);
        Customer secondWaitlistedCustomer = createCustomer("second-" + suffix);

        com.anurag.appointment.entity.Service service = new com.anurag.appointment.entity.Service();
        service.setServiceName("Workflow test service");
        service.setDescription("Temporary integration-test record");
        service.setDuration(30);
        service.setPrice(1.0);
        service.setProvider(provider);
        service = serviceRepository.save(service);

        LocalDate date = LocalDate.now().plusDays(3);
        Slot slot = new Slot();
        slot.setService(service);
        slot.setProvider(provider);
        slot.setDate(date);
        slot.setStartTime(LocalTime.of(10, 0));
        slot.setEndTime(LocalTime.of(10, 30));
        slot.setStatus("AVAILABLE");
        slot = slotRepository.save(slot);

        createWaitlistEntry(firstWaitlistedCustomer, service, date, LocalTime.of(10, 0), LocalTime.of(10, 30));
        createWaitlistEntry(secondWaitlistedCustomer, service, date, LocalTime.of(11, 0), LocalTime.of(11, 30));

        Appointment booked = new Appointment();
        booked.setCustomer(cancellingCustomer);
        booked.setSlot(slot);
        booked = appointmentService.createAppointment(booked, cancellingCustomer.getId());

        Match firstOffer = appointmentService.cancelAppointment(booked.getId(), cancellingCustomer.getId(), "customer");
        assertNotNull(firstOffer);
        assertEquals(firstWaitlistedCustomer.getId(), firstOffer.getWaitlist().getCustomer().getId());
        assertEquals(100, firstOffer.getTotalScore());
        assertEquals("AVAILABLE", slotService.getSlotById(slot.getId()).getStatus());

        Match secondOffer = matchService.rejectMatch(firstOffer.getId(), firstWaitlistedCustomer.getId());
        assertNotNull(secondOffer);
        assertEquals(secondWaitlistedCustomer.getId(), secondOffer.getWaitlist().getCustomer().getId());
        assertEquals(70, secondOffer.getTotalScore());

        Match accepted = matchService.acceptMatch(secondOffer.getId(), secondWaitlistedCustomer.getId());
        assertEquals("ACCEPTED", accepted.getStatus());
        assertEquals("MATCHED", waitlistService.getWaitlistById(secondOffer.getWaitlist().getId()).getStatus());
        assertEquals("BOOKED", slotService.getSlotById(slot.getId()).getStatus());
        assertEquals(1, appointmentRepository.findByCustomerId(secondWaitlistedCustomer.getId()).size());
        assertEquals("REJECTED", matchRepository.findById(firstOffer.getId()).orElseThrow().getStatus());
    }

    @Test
    void apiRequiresAuthenticationAndEnforcesAccountRoles() throws Exception {
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register/customer")
                        .contentType("application/json")
                        .content("""
                                {"name":"Invalid password test","email":"invalid-password-%s@example.test",
                                 "password":"%s","phone":""}
                                """.formatted(UUID.randomUUID(), "é".repeat(40))))
                .andExpect(status().isBadRequest());

        String registration = """
                {"name":"Security test customer","email":"security-%s@example.test",
                 "password":"secure-test-password-123","phone":""}
                """.formatted(UUID.randomUUID());
        String tokenResponse = mockMvc.perform(post("/api/auth/register/customer")
                        .contentType("application/json")
                        .content(registration))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.role").value("customer"))
                .andReturn().getResponse().getContentAsString();
        String token = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(tokenResponse).get("accessToken").asText();

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("customer"));

        mockMvc.perform(get("/api/slots").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/appointments").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private Customer createCustomer(String emailPrefix) {
        Customer customer = new Customer();
        customer.setName("Workflow test customer");
        customer.setEmail(emailPrefix + "@example.test");
        customer.setPassword("test-only");
        return customerRepository.save(customer);
    }

    private void createWaitlistEntry(Customer customer, com.anurag.appointment.entity.Service service,
                                     LocalDate date, LocalTime start, LocalTime end) {
        Waitlist entry = new Waitlist();
        entry.setCustomer(customer);
        entry.setService(service);
        entry.setPreferredDate(date);
        entry.setPreferredStartTime(start);
        entry.setPreferredEndTime(end);
        waitlistService.createWaitlistEntry(entry);
    }
}
