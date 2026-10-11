package com.anurag.appointment.controller;

import com.anurag.appointment.entity.Waitlist;
import com.anurag.appointment.service.WaitlistService;
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
@RequestMapping("/api/waitlist")
public class WaitlistController {
    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','PROVIDER')")
    public List<Waitlist> getMyWaitlist(@AuthenticationPrincipal Jwt jwt) {
        Long accountId = JwtAccount.id(jwt);
        return "CUSTOMER".equals(jwt.getClaimAsString("role").toUpperCase())
                ? waitlistService.getWaitlistEntriesForCustomer(accountId)
                : waitlistService.getWaitlistEntriesForProvider(accountId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Waitlist getWaitlistById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return waitlistService.getWaitlistEntriesForCustomer(JwtAccount.id(jwt)).stream()
                .filter(entry -> entry.getId().equals(id)).findFirst()
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Waitlist entry not found"));
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public Waitlist createWaitlistEntry(@RequestBody Waitlist waitlist, @AuthenticationPrincipal Jwt jwt) {
        waitlist.setCustomer(new com.anurag.appointment.entity.Customer());
        waitlist.getCustomer().setId(JwtAccount.id(jwt));
        return waitlistService.createWaitlistEntry(waitlist);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Waitlist updateWaitlistEntry(@PathVariable Long id, @RequestBody Waitlist waitlist, @AuthenticationPrincipal Jwt jwt) {
        return waitlistService.updateCustomerEntry(id, waitlist, JwtAccount.id(jwt));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public void deleteWaitlistEntry(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        waitlistService.deleteWaitlistEntry(id, JwtAccount.id(jwt));
    }
}
