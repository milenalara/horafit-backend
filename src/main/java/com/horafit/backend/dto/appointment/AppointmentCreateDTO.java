package com.horafit.backend.dto.appointment;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AppointmentCreateDTO {
    private String data;
    private String horario;
    private RepetirDTO repetir;
    private String modalidade;
    private List<Long> clientes;
    private Long fisioterapeuta;
    private String locacao;
}
