package com.anurag.appointment.service;

import com.anurag.appointment.entity.Appointment;
import com.anurag.appointment.entity.Match;
import com.anurag.appointment.entity.Service;
import com.anurag.appointment.entity.Slot;
import com.anurag.appointment.entity.Waitlist;
import com.anurag.appointment.repository.AppointmentRepository;
import com.anurag.appointment.repository.MatchRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@org.springframework.stereotype.Service
public class MatchService {
    private final MatchRepository matchRepository;
    private final AppointmentRepository appointmentRepository;
    private final WaitlistService waitlistService;
    private final SlotService slotService;

    public MatchService(MatchRepository matchRepository, AppointmentRepository appointmentRepository, WaitlistService waitlistService, SlotService slotService) {
        this.matchRepository = matchRepository;
        this.appointmentRepository = appointmentRepository;
        this.waitlistService = waitlistService;
        this.slotService = slotService;
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    public List<Match> getMatchesForCustomer(Long customerId) {
        return matchRepository.findByWaitlistCustomerId(customerId);
    }

    public Match getMatchById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));
    }

    @Transactional
    public Match createMatch(Match match) {
        return matchRepository.save(match);
    }

    @Transactional
    public Match createBestMatchForCancelledSlot(Slot slot) {
        if (slot == null) {
            return null;
        }

        slot = slotService.getSlotForUpdate(slot.getId());
        Service service = slot.getService();
        if (service == null) {
            return null;
        }

        List<Waitlist> activeWaitlist = waitlistService.getActiveWaitlistEntriesForService(service.getId());
        if (activeWaitlist == null || activeWaitlist.isEmpty()) {
            return null;
        }

        if (!matchRepository.findBySlotIdAndStatus(slot.getId(), "PENDING").isEmpty()) {
            return null;
        }

        Match bestMatch = null;
        for (Waitlist waitlistEntry : activeWaitlist) {
            if (waitlistEntry == null || waitlistEntry.getCustomer() == null
                    || waitlistEntry.getService() == null
                    || matchRepository.existsBySlotIdAndWaitlistId(slot.getId(), waitlistEntry.getId())
                    || matchRepository.existsByWaitlistIdAndStatus(waitlistEntry.getId(), "PENDING")) {
                continue;
            }

            int serviceScore = Objects.equals(waitlistEntry.getService().getId(), service.getId()) ? 40 : 0;
            int dateScore = Objects.equals(waitlistEntry.getPreferredDate(), slot.getDate()) ? 30 : 0;
            int timeScore = Objects.equals(waitlistEntry.getPreferredStartTime(), slot.getStartTime())
                    && Objects.equals(waitlistEntry.getPreferredEndTime(), slot.getEndTime()) ? 30 : 0;
            if (dateScore == 0) {
                continue;
            }
            int totalScore = serviceScore + dateScore + timeScore;

            if (totalScore <= 0) {
                continue;
            }

            Match candidate = new Match();
            candidate.setWaitlist(waitlistEntry);
            candidate.setSlot(slot);
            candidate.setServiceScore(serviceScore);
            candidate.setDateScore(dateScore);
            candidate.setTimeScore(timeScore);
            candidate.setTotalScore(totalScore);
            candidate.setStatus("PENDING");

            if (bestMatch == null
                    || candidate.getTotalScore() > bestMatch.getTotalScore()
                    || (candidate.getTotalScore().equals(bestMatch.getTotalScore())
                    && candidate.getWaitlist().getId() < bestMatch.getWaitlist().getId())) {
                bestMatch = candidate;
            }
        }

        if (bestMatch == null) {
            return null;
        }

        return matchRepository.save(bestMatch);
    }

    @Transactional
    public Match offerNextForSlot(Long slotId, Long providerId) {
        Slot slot = slotService.getSlotForProvider(slotId, providerId);
        if (!"AVAILABLE".equals(slot.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot is not available for a waitlist offer");
        }
        if (!matchRepository.findBySlotIdAndStatus(slotId, "PENDING").isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This slot already has a pending offer");
        }
        return createBestMatchForCancelledSlot(slot);
    }

    @Transactional
    public Match acceptMatch(Long matchId, Long customerId) {
        Match match = getMatchById(matchId);
        ensureCustomerOwnsMatch(match, customerId);
        if ("ACCEPTED".equals(match.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offer already accepted");
        }
        if ("REJECTED".equals(match.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offer already rejected");
        }

        Waitlist waitlist = match.getWaitlist();
        Slot slot = slotService.getSlotForUpdate(match.getSlot().getId());
        match.setSlot(slot);
        if (waitlist == null || slot == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Match is incomplete");
        }
        if (!"AVAILABLE".equals(slot.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Offered slot is no longer available");
        }

        Appointment appointment = new Appointment();
        appointment.setCustomer(waitlist.getCustomer());
        appointment.setService(waitlist.getService());
        appointment.setProvider(slot.getProvider());
        appointment.setSlot(slot);
        appointment.setAppointmentDate(slot.getDate());
        appointment.setStartTime(slot.getStartTime());
        appointment.setEndTime(slot.getEndTime());
        appointment.setStatus("BOOKED");

        slot.setStatus("BOOKED");
        waitlist.setStatus("MATCHED");
        match.setStatus("ACCEPTED");

        appointmentRepository.save(appointment);
        matchRepository.save(match);
        return match;
    }

    @Transactional
    public Match rejectMatch(Long matchId, Long customerId) {
        Match match = getMatchById(matchId);
        ensureCustomerOwnsMatch(match, customerId);
        if ("ACCEPTED".equals(match.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Accepted offer cannot be rejected");
        }
        if ("REJECTED".equals(match.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offer already rejected");
        }

        match.setStatus("REJECTED");
        matchRepository.save(match);

        Slot slot = match.getSlot();
        if (slot != null) {
            return createBestMatchForCancelledSlot(slot);
        }
        return match;
    }

    private void ensureCustomerOwnsMatch(Match match, Long customerId) {
        if (match.getWaitlist() == null || match.getWaitlist().getCustomer() == null
                || !match.getWaitlist().getCustomer().getId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found");
        }
    }
}
