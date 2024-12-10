package com.horafit.backend.dto.appointmentRules;

public record CanRescheduleDTO(
    Boolean canReschedule,
    String reason, // motivo pelo qual o cliente nao pode reagendar
    String message // explicacao mais detalhada sobre reason, que vai aparecer no modal do front
) {
}
