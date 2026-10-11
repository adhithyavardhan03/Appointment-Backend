package com.anurag.appointment.repository;

import com.anurag.appointment.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByStatus(String status);
    List<Slot> findByProviderIdAndStatus(Long providerId, String status);
    List<Slot> findByProviderId(Long providerId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Slot s where s.id = :id")
    java.util.Optional<Slot> findByIdForUpdate(@Param("id") Long id);
}
