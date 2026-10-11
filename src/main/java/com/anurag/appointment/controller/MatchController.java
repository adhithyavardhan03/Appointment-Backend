package com.anurag.appointment.controller;

import com.anurag.appointment.entity.Match;
import com.anurag.appointment.service.MatchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import com.anurag.appointment.security.JwtAccount;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<Match> getMyMatches(@AuthenticationPrincipal Jwt jwt) {
        return matchService.getMatchesForCustomer(JwtAccount.id(jwt));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Match getMatchById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Match match = matchService.getMatchById(id);
        if (!match.getWaitlist().getCustomer().getId().equals(JwtAccount.id(jwt))) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found");
        }
        return match;
    }

    @PostMapping("/slot/{slotId}/offer")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Match> offerNextForSlot(@PathVariable Long slotId, @AuthenticationPrincipal Jwt jwt) {
        Match offer = matchService.offerNextForSlot(slotId, JwtAccount.id(jwt));
        return offer == null ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.CREATED).body(offer);
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Match acceptMatch(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return matchService.acceptMatch(id, JwtAccount.id(jwt));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Match rejectMatch(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return matchService.rejectMatch(id, JwtAccount.id(jwt));
    }
}
