package com.horafit.backend.dto.appointment;

import com.horafit.backend.entity.enums.AppointmentModality;
import com.horafit.backend.entity.enums.AppointmentLocation;

import java.time.LocalDateTime;
import java.util.List;

public record AppointmentGetDTO(
        Long id,
        LocalDateTime dateTime,
        AppointmentLocation location,
        AppointmentModality modality,
        AppointmentPhysiotherapistDTO physiotherapist,
        List<AppointmentClientDTO> clients
) {
}
