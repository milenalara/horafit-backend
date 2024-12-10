package com.horafit.backend.repository;

import ch.qos.logback.core.net.server.Client;
import com.horafit.backend.entity.Appointment;
import com.horafit.backend.entity.AppointmentClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link AppointmentClient} entities.
 * Provides methods for querying and interacting with appointment-client
 * relationships
 * in the database, including both JPA-derived methods and custom SQL queries.
 */
@Repository
public interface AppointmentClientRepository extends JpaRepository<AppointmentClient, Long> {
        @Query(value = "SELECT ac.* FROM appointment_client AS ac " +
        "JOIN appointment AS a " +
        "ON a.id = ac.appointment_id " +
        "WHERE client_id = :clientId " +
        "AND (DATE_FORMAT(a.date_time, '%m/%Y') = DATE_FORMAT(CURDATE(), '%m/%Y') " +
        "OR DATE_FORMAT(a.date_time, '%m/%Y') = DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 1 MONTH), '%m/%Y'));", nativeQuery = true)
        List<AppointmentClient> findByClientIdInCurrentAndNextMonth(@Param("clientId") Long clientId);

        /**
         * Finds all {@link AppointmentClient} entities associated with a specific
         * {@link Appointment}.
         *
         * @param appointment the appointment entity.
         * @return a list of {@link AppointmentClient} entities related to the given
         *         appointment.
         */
        List<AppointmentClient> findByAppointment(Appointment appointment);

        /**
         * Finds all {@link AppointmentClient} entities associated with a specific
         * client ID.
         *
         * @param clientId the ID of the client.
         * @return a list of {@link AppointmentClient} entities related to the given
         *         client ID.
         */
        List<AppointmentClient> findByClient_Id(Long clientId);

        /**
         * Finds a single {@link AppointmentClient} entity by appointment ID and client
         * ID.
         *
         * @param appointmentId the ID of the appointment.
         * @param clientId      the ID of the client.
         * @return an {@link Optional} containing the {@link AppointmentClient} if
         *         found, or empty otherwise.
         */
        Optional<AppointmentClient> findByAppointmentIdAndClientId(Long appointmentId, Long clientId);

        /**
         * Deletes all {@link AppointmentClient} entities associated with a specific
         * client ID.
         *
         * @param clientId the ID of the client whose appointments should be deleted.
         */
        @Modifying
        @Query(value = "DELETE FROM horafit.appointment_client WHERE client_id = :clientId", nativeQuery = true)
        void deleteByClientId(@Param("clientId") Long clientId);

        /**
         * Counts the number of clients associated with a specific appointment.
         *
         * @param appointmentId the ID of the appointment.
         * @return the count of clients associated with the given appointment.
         */
        @Query(value = "SELECT COUNT(*) FROM horafit.appointment_client ac WHERE ac.appointment_id = :appointmentId", nativeQuery = true)
        Long countByAppointmentId(@Param("appointmentId") Long appointmentId);

        /**
         * Finds all future {@link AppointmentClient} entities for a specific client ID,
         * ordered by appointment date and time.
         *
         * @param id the ID of the client.
         * @return a list of future {@link AppointmentClient} entities for the given
         *         client ID.
         */
        @Query(value = "SELECT " +
                        "ac.id, " +
                        "ac.client_id, " +
                        "ac.appointment_id, " +
                        "a.date_time, " +
                        "a.location, " +
                        "a.modality, " +
                        "a.physiotherapist_id, " +
                        "ac.attendance, " +
                        "ac.confirmation " +
                        "FROM horafit.appointment_client AS ac " +
                        "JOIN horafit.appointment AS a " +
                        "ON ac.appointment_id = a.id " +
                        "JOIN horafit.client AS c " +
                        "ON ac.client_id = c.id " +
                        "WHERE c.id = :id " +
                        "AND a.date_time >= NOW() " +
                        "ORDER BY a.date_time", nativeQuery = true)
        List<AppointmentClient> findFutureAppointmentsByClientId(@Param("id") Long id);

        /**
         * Finds a single {@link AppointmentClient} entity by client ID and appointment
         * ID.
         *
         * @param clientId      the ID of the client.
         * @param appointmentId the ID of the appointment.
         * @return an {@link Optional} containing the {@link AppointmentClient} if
         *         found, or empty otherwise.
         */
        Optional<AppointmentClient> findAppointmentClientByClient_IdAndAppointment_Id(Long clientId,
                        Long appointmentId);

        /**
         * Checks if a client has an appointment at a specific date and time.
         *
         * @param clientId the ID of the client.
         * @param dateTime the date and time of the appointment.
         * @return a boolean value indicating whether the client has an appointment at
         *         the given date and time.
         */
        @Query(value = "SELECT COUNT(*) > 0 FROM horafit.appointment_client ac " +
                        "JOIN horafit.appointment a ON ac.appointment_id = a.id " +
                        "WHERE ac.client_id = :clientId AND a.date_time = :dateTime", nativeQuery = true)
        Long existsByClientIdAndDateTime(@Param("clientId") Long clientId, @Param("dateTime") LocalDateTime dateTime);

        /**
         * Checks if a physiotherapist has an appointment at a specific date and time.
         *
         * @param physiotherapistId the ID of the physiotherapist.
         * @param dateTime          the date and time of the appointment.
         * @return a boolean value indicating whether the physiotherapist has an
         *         appointment at the given date and time.
         */
        @Query(value = "SELECT COUNT(*) > 0 FROM horafit.appointment a " +
                        "WHERE a.physiotherapist_id = :physiotherapistId AND a.date_time = :dateTime", nativeQuery = true)
        Long existsByPhysiotherapistIdAndDateTime(@Param("physiotherapistId") Long physiotherapistId,
                        @Param("dateTime") LocalDateTime dateTime);

        boolean existsByAppointmentIdAndClientId(Long appointmentId, Long clientId);
}
