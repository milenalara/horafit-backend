package com.horafit.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.horafit.backend.entity.enums.AppointmentFrequency;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "appointment_rules")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AppointmentRules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Column(name = "rule_name", nullable = false)
    private String name;

    @Column(name = "reescheduling_limit", nullable = false)
    private int reeschedulingLimit;

    @Column(name = "reescheduling_min_hours_in_advance", nullable = false)
    private int reeschedulingMinHoursInAdvance;

    @Column(name = "max_clients_per_group", length = 45, nullable = false)
    private int maxClientsPerGroup;

    @Column(name= "frequency", nullable = false)
    private AppointmentFrequency frequency;

    @OneToMany(mappedBy = "appointmentRules",orphanRemoval = true)
    @JsonProperty(access = Access.WRITE_ONLY)
    private List<Client> clients;
}
