package com.horafit.backend.dto.appointment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentUpdateDTO {
    private Long idAtendimento;
    private Long idCliente;
    private String data;
    private String horario;
    private String modalidade;
}
