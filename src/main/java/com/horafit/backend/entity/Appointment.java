package com.horafit.backend.entity;

import com.horafit.backend.entity.enums.AppointmentModality;
import com.horafit.backend.entity.enums.AppointmentLocation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false, unique = true)
  private Long id;

  @Column(name = "date_time", nullable = false)
  private LocalDateTime dateTime;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AppointmentLocation location;

  @ManyToOne
  @JoinColumn(name = "physiotherapist_id", nullable = false)
  private Physiotherapist physiotherapist;

  @Enumerated(EnumType.STRING)
  @Column(name = "modality", nullable = false)
  private AppointmentModality modality;
}
