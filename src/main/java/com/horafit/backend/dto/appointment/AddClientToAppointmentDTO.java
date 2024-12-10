package com.horafit.backend.dto.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class AddClientToAppointmentDTO {
    private Long appointmentId;
    private List<Long> clientIds;
}
