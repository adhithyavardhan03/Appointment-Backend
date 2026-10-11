package com.anurag.appointment.repository;

import com.anurag.appointment.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
    Optional<Provider> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
