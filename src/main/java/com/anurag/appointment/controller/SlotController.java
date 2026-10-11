package com.anurag.appointment.controller;

import com.anurag.appointment.entity.Slot;
import com.anurag.appointment.service.SlotService;
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
@RequestMapping("/api/slots")
public class SlotController {
    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public List<Slot> getProviderSlots(@AuthenticationPrincipal Jwt jwt) {
        return slotService.getSlotsForProvider(JwtAccount.id(jwt));
    }

    @GetMapping("/available")
    public List<Slot> getAvailableSlots() {
        return slotService.getAvailableSlots();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public Slot getSlotById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return slotService.getSlotForProvider(id, JwtAccount.id(jwt));
    }

    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public Slot createSlot(@RequestBody Slot slot, @AuthenticationPrincipal Jwt jwt) {
        return slotService.createSlot(slot, JwtAccount.id(jwt));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public Slot updateSlot(@PathVariable Long id, @RequestBody Slot slot, @AuthenticationPrincipal Jwt jwt) {
        return slotService.updateSlot(id, slot, JwtAccount.id(jwt));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public void deleteSlot(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        slotService.deleteSlot(id, JwtAccount.id(jwt));
    }
}
