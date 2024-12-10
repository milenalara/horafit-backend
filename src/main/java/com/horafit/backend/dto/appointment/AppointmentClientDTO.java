package com.horafit.backend.dto.appointment;

import com.horafit.backend.entity.enums.AppointmentConfirmation;

public record AppointmentClientDTO(
    Long clientId,
    String name,
    AppointmentConfirmation confirmation,
    Boolean attendance
) {
}
