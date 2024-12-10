package com.horafit.backend.repository;

import com.horafit.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query(value = "SELECT * FROM horafit.payment WHERE client_id = :clientId ORDER BY confirmed DESC LIMIT 1", nativeQuery = true)
    Optional<Payment> findLatestPaymentByClientId(Long clientId);

    Optional<Payment> findByClientId(Long clientId);
}
