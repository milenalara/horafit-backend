package com.horafit.backend.dto.appointmentRules;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentRulesScheduleDTO {
    private Long id;
    private String name;
    private String frequency;
}