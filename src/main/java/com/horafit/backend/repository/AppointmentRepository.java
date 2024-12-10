package com.horafit.backend.repository;

import ch.qos.logback.core.net.server.Client;
import com.horafit.backend.entity.Appointment;
import com.horafit.backend.entity.Physiotherapist;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository interface for managing {@link Appointment} entities.
 * Provides methods for querying and interacting with appointment data,
 * including JPA-derived methods and custom SQL queries.
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

        /**
         * Finds all appointments associated with a specific physiotherapist.
         *
         * @param id the ID of the physiotherapist.
         * @return a list of {@link Appointment} entities related to the given
         *         physiotherapist.
         */
        List<Appointment> findByPhysiotherapist_Id(Long id);

        // pensar se vamos manter este metodo, ou se vamos substituir pelo de baixo
        /**
         * Finds appointments based on date, time, and physiotherapist using JPA-derived
         * queries.
         *
         * @param dateTime        the date and time of the appointment.
         * @param physiotherapist the physiotherapist entity.
         * @return a list of {@link Appointment} entities matching the criteria.
         */
        List<Appointment> findByDateTimeAndPhysiotherapist(LocalDateTime dateTime, Physiotherapist physiotherapist);

        /**
         * Finds an appointment by physiotherapist ID and date/time using a native SQL
         * query.
         *
         * @param physiotherapistId the ID of the physiotherapist.
         * @param dateTime          the date and time of the appointment.
         * @return the {@link Appointment} entity matching the criteria.
         */
        @Query(value = "SELECT * FROM horafit.appointment " +
                        "WHERE date_time = :dateTime " +
                        "AND physiotherapist_id = :physiotherapistId;", nativeQuery = true)
        Appointment findByDateTimeAndPhysiotherapist(Long physiotherapistId, LocalDateTime dateTime);

        /**
         * Finds appointments by client ID, modality, and month/year using a native SQL
         * query.
         *
         * @param clientId  the ID of the client.
         * @param modality  the modality of the appointment.
         * @param monthYear the month and year in the format "MM/YYYY".
         * @return a list of {@link Appointment} entities matching the criteria.
         */
        @Query(value = "SELECT a.* FROM horafit.appointment a " +
                        "JOIN horafit.appointment_client ac ON a.id = ac.appointment_id " +
                        "WHERE ac.client_id = :clientId " +
                        "AND a.modality = :modality " +
                        "AND DATE_FORMAT(a.date_time, '%m/%Y') = :monthYear", nativeQuery = true)
        List<Appointment> findByClientIdAndModalityAndMonthYear(@Param("clientId") Long clientId,
                        @Param("modality") String modality,
                        @Param("monthYear") String monthYear);

        /**
         * Finds appointments by client ID and modality using a native SQL query.
         *
         * @param clientId the ID of the client.
         * @param modality the modality of the appointment.
         * @return a list of {@link Appointment} entities matching the criteria.
         */
        @Query(value = "SELECT a.* FROM horafit.appointment a JOIN horafit.appointment_client ac ON a.id = ac.appointment_id WHERE ac.client_id = :clientId AND a.modality = :modality;", nativeQuery = true)
        List<Appointment> findByClientIdAndModality(@Param("clientId") Long clientId,
                        @Param("modality") String modality);

        /**
         * Finds appointments by client ID and month/year using a native SQL query.
         *
         * @param clientId  the ID of the client.
         * @param monthYear the month and year in the format "MM/YYYY".
         * @return a list of {@link Appointment} entities matching the criteria.
         */
        @Query(value = "SELECT a.* FROM horafit.appointment a " +
                        "JOIN horafit.appointment_client ac ON a.id = ac.appointment_id " +
                        "WHERE ac.client_id = :clientId " +
                        "AND DATE_FORMAT(a.date_time, '%m/%Y') = :monthYear", nativeQuery = true)
        List<Appointment> findByClientIdAndMonthYear(@Param("clientId") Long clientId,
                        @Param("monthYear") String monthYear);

        /**
         * Finds all appointments associated with a specific client ID using a native
         * SQL query.
         *
         * @param clientId the ID of the client.
         * @return a list of {@link Appointment} entities related to the given client
         *         ID.
         */
        @Query(value = "SELECT a.* FROM horafit.appointment a JOIN horafit.appointment_client ac WHERE ac.client_id = :clientId;", nativeQuery = true)
        List<Appointment> findByClientId(@Param("clientId") Long clientId);

        /**
         * Finds appointments scheduled for a specific date using a native SQL query.
         *
         * @param date the date of the appointments.
         * @return a list of {@link Appointment} entities scheduled on the given date.
         */
        @Query(value = "SELECT * FROM horafit.appointment WHERE DATE(date_time) = :date;", nativeQuery = true)
        List<Appointment> findByDate(LocalDate date);

        /**
         * Finds available appointments for a specific client, ensuring the client is
         * not already
         * associated with the appointments and the number of confirmed or rescheduled
         * clients is below the maximum.
         *
         * @param clientId the ID of the client.
         * @return a list of available {@link Appointment} entities.
         */
        @Query(value = "SELECT a.*\n" +
                        "FROM appointment AS a\n" +
                        "JOIN appointment_client AS ac ON ac.appointment_id = a.id\n" +
                        "WHERE a.date_time >= NOW()\n" +
                        "AND a.id NOT IN (\n" +
                        "    SELECT ac2.appointment_id\n" +
                        "    FROM appointment_client AS ac2\n" +
                        "    WHERE ac2.client_id = :clientId\n" +
                        ")\n" +
                        "GROUP BY a.id\n" +
                        "HAVING COUNT(CASE WHEN ac.confirmation = 'CONFIRMED' " +
                        "OR ac.confirmation = 'RESCHEDULED' THEN 1 END) < 4 " +
                        "ORDER BY a.date_time;", nativeQuery = true)
        List<Appointment> findAvailable(Long clientId);

        /**
         * Finds all appointments associated with a specific client using a native SQL
         * query.
         *
         * @param clientId the ID of the client.
         * @return a list of {@link Appointment} entities associated with the client.
         */
        @Query(value = "SELECT a.* FROM horafit.appointment a " +
                        "JOIN horafit.appointment_client ac ON ac.appointment_id = a.id " +
                        "WHERE ac.client_id = :clientId", nativeQuery = true)
        List<Appointment> findAppointmentsByClientId(@Param("clientId") Long clientId);

        /**
         * Deletes all {@link Appointment} entities associated with a specific client
         * ID.
         *
         * @param clientId the ID of the client whose appointments should be deleted.
         */
        @Modifying
        @Query(value = "DELETE FROM horafit.appointment a " +
                        "USING horafit.appointment_client ac " +
                        "WHERE a.id = ac.appointment_id " +
                        "AND ac.client_id = :clientId", nativeQuery = true)
        void deleteByClientId(@Param("clientId") Long clientId);
}