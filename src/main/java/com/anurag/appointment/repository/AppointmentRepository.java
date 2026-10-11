package com.anurag.appointment.repository;

import com.anurag.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByStatus(String status);
    List<Appointment> findByCustomerId(Long customerId);
    List<Appointment> findByProviderId(Long providerId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Appointment a where a.id = :id")
    java.util.Optional<Appointment> findByIdForUpdate(@Param("id") Long id);
}
