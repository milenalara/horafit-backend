package com.horafit.backend.dto.appointmentRules;

import com.horafit.backend.entity.enums.AppointmentFrequency;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentRulesDTO{
    private Long id;
    private String name;
    private int reeschedulingLimit;
    private int reeschedulingMinHoursInAdvance;
    private int maxClientsPerGroup;
    private AppointmentFrequency frequency;
}
