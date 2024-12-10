package com.horafit.backend.entity;

import com.horafit.backend.entity.enums.AppointmentConfirmation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "appointment_client")
@Getter
@Setter
public class AppointmentClient {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne
  @JoinColumn(name = "appointment_id")
  Appointment appointment;

  @ManyToOne
  @JoinColumn(name = "client_id")
  Client client;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  AppointmentConfirmation confirmation;

  @Column(columnDefinition = "TINYINT(1)", nullable = false)
  Boolean attendance;
}
