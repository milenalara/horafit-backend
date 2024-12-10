package com.horafit.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.horafit.backend.entity.AppointmentClient;
import com.horafit.backend.service.AppointmentClientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@CrossOrigin
@RequestMapping("/api/appointment-client")
@Tag(name = "Appointment Client API", description = "API para operações relacionadas ao vínculo entre um cliente e um atendimento.")
public class AppointmentClientController {
    @Autowired
    AppointmentClientService appointmentClientService;
    
    @Operation(summary = "Registrar ausência do cliente.",
          description = "Este endpoint permite informar a ausência do cliente sem informar um aviso prévio.")
    @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "Cliente marcado como ausente com sucesso"),
          @ApiResponse(responseCode = "404", description = "Atendimento não encontrado")
    })
    @PutMapping("/absent-client/{clientId}/{appointmentId}")
    public ResponseEntity<AppointmentClient> absentClient(
            @PathVariable Long clientId,
            @PathVariable Long appointmentId) {
        AppointmentClient appointmentClient = appointmentClientService.absentClient(clientId, appointmentId);
        return ResponseEntity.ok(appointmentClient);
    }
}
