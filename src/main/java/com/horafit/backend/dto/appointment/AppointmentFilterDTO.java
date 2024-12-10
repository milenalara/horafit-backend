package com.horafit.backend.dto.appointment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentFilterDTO {
  Long clientId;
  String modality;
  String monthYear;
}