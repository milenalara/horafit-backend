package com.horafit.backend.dto.appointment;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class DayScheduleDTO {
    private String dia;
    private List<LocalTime> horarios;
}