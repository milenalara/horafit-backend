package com.horafit.backend.dto.client;

import com.horafit.backend.dto.appointment.AppointmentDTO;
import com.horafit.backend.dto.appointmentRules.AppointmentRulesScheduleDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClientScheduleDTO {
    private String nome;
    private String aceiteContrato;
    private String ultimoPagamento;
    private List<AppointmentDTO> atendimentos;
    private AppointmentRulesScheduleDTO regrasDeRemarcacao;
}

