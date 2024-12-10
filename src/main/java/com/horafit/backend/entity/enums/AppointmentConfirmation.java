package com.horafit.backend.entity.enums;

public enum AppointmentConfirmation {
  CONFIRMED,
  CANCELED_WITH_RESCHEDULING, // cancelado e pode ser reagendado
  CANCELED_WITHOUT_RESCHEDULING, // cancelado e NAO pode ser reagendado
  RESCHEDULED
}