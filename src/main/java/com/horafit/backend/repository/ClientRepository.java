package com.horafit.backend.repository;

import com.horafit.backend.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query(value = "SELECT DISTINCT c.* FROM horafit.client c JOIN horafit.appointment_client a ON c.id = a.client_id WHERE c.name LIKE %:name%", nativeQuery = true)
    List<Client> findClientsWithAppointmentsByName(String name);

    @Query(value = "SELECT DISTINCT c.* FROM horafit.client c JOIN horafit.appointment_client a ON c.id = a.client_id", nativeQuery = true)
    List<Client> findClientsWithAppointments();

    @Query(value = "SELECT * FROM horafit.client ORDER BY name;", nativeQuery = true)
    List<Client> findAll();
}
