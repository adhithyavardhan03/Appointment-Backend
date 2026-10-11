package com.anurag.appointment.repository;

import com.anurag.appointment.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    List<Waitlist> findByStatus(String status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Waitlist w where w.service.id = :serviceId and w.status = :status order by w.id")
    List<Waitlist> findActiveCandidatesForUpdate(@Param("serviceId") Long serviceId, @Param("status") String status);
    List<Waitlist> findByCustomerId(Long customerId);
    List<Waitlist> findByServiceProviderId(Long providerId);
}
