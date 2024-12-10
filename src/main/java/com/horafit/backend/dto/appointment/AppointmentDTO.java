package com.horafit.backend.dto.appointment;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AppointmentDTO {
    private String modalidade;
    private List<DayScheduleDTO> diasDaSemana;
}