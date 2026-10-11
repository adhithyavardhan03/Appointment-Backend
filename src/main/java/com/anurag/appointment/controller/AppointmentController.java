package com.anurag.appointment.controller;

import com.anurag.appointment.entity.Appointment;
import com.anurag.appointment.entity.Match;
import com.anurag.appointment.dto.CancellationResult;
import com.anurag.appointment.service.AppointmentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import com.anurag.appointment.security.JwtAccount;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','PROVIDER')")
    public List<Appointment> getMyAppointments(@AuthenticationPrincipal Jwt jwt) {
        Long accountId = JwtAccount.id(jwt);
        return "CUSTOMER".equals(jwt.getClaimAsString("role").toUpperCase())
                ? appointmentService.getAppointmentsForCustomer(accountId)
                : appointmentService.getAppointmentsForProvider(accountId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Appointment getAppointmentById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return appointmentService.getAppointmentForCustomer(id, JwtAccount.id(jwt));
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public Appointment createAppointment(@RequestBody Appointment appointment, @AuthenticationPrincipal Jwt jwt) {
        return appointmentService.createAppointment(appointment, JwtAccount.id(jwt));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Appointment updateAppointment(@PathVariable Long id, @RequestBody Appointment appointment, @AuthenticationPrincipal Jwt jwt) {
        return appointmentService.updateAppointment(id, appointment, JwtAccount.id(jwt));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public void deleteAppointment(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        appointmentService.deleteAppointment(id, JwtAccount.id(jwt));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','PROVIDER')")
    public CancellationResult cancelAppointment(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Match offer = appointmentService.cancelAppointment(id, JwtAccount.id(jwt), jwt.getClaimAsString("role"));
        return new CancellationResult(
                true,
                offer == null ? "Appointment cancelled. No eligible waitlist customer was found."
                        : "Appointment cancelled and a recovered-slot offer was created.",
                offer
        );
    }
}
