package com.horafit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.horafit.backend.entity.AppointmentRules;
import java.util.Optional;

@Repository
public interface AppointmentRulesRepository extends JpaRepository<AppointmentRules, Long> {
  @Query(value = "SELECT reescheduling_min_hours_in_advance FROM horafit.appointment_rules AS ar\n" +
      "JOIN horafit.client AS c " +
      "ON c.appointment_rules = ar.id " +
      "WHERE c.id = :clientId;", nativeQuery = true)
  Long findReschedulingMinHoursInAdvance(@Param("clientId") Long clientId);

  Optional<AppointmentRules> findById(Long id);

  @Query(value = "SELECT COUNT(*) FROM horafit.appointment AS a " +
      "JOIN horafit.appointment_client AS ac " +
      "ON ac.appointment_id = a.id " +
      "JOIN horafit.client as c " +
      "ON ac.client_id = c.id " +
      "WHERE c.id = :id " +
      "AND ac.confirmation = 'RESCHEDULED' " +
      "AND DATE_FORMAT(a.date_time, '%m/%Y') = DATE_FORMAT(CURDATE(), '%m/%Y') "
      , nativeQuery = true)
  Integer countRescheduledAppointmentsInMonth(Long id);
}
