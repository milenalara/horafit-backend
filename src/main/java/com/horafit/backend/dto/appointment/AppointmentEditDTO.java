package com.horafit.backend.dto.appointment;

import com.horafit.backend.entity.enums.AppointmentModality;
import com.horafit.backend.entity.enums.AppointmentLocation;

import java.time.LocalDateTime;

public record AppointmentEditDTO(
    LocalDateTime dateTime,
    AppointmentLocation location,
    AppointmentModality modality,
    Long physiotherapistId
) {
}
