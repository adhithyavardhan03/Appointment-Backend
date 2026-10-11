package com.anurag.appointment.repository;

import com.anurag.appointment.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByStatus(String status);
    List<Match> findBySlotId(Long slotId);
    List<Match> findBySlotIdAndStatus(Long slotId, String status);
    boolean existsBySlotIdAndWaitlistId(Long slotId, Long waitlistId);
    boolean existsByWaitlistIdAndStatus(Long waitlistId, String status);
    List<Match> findByWaitlistCustomerId(Long customerId);
    List<Match> findByWaitlistCustomerIdAndStatus(Long customerId, String status);
}
