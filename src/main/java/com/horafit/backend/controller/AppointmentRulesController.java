package com.horafit.backend.controller;

import com.horafit.backend.dto.appointmentRules.CanRescheduleDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.horafit.backend.dto.appointmentRules.AppointmentRulesDTO;
import com.horafit.backend.service.AppointmentRulesService;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/appointmentRules")
@CrossOrigin
@Tag(name = "Appointment Rules API", description = "Operações relacionadas às regras para cancelamento e remarcação de sessões")
public class AppointmentRulesController {
    @Autowired
    AppointmentRulesService appointmentRulesService;


    @Operation(summary = "Registrar nova regra de remarcação", description = "Este endpoint cadastra uma nova regra no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cadastro realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody AppointmentRulesDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            result.getAllErrors().forEach(error -> errors.append(error.getDefaultMessage()).append(". "));
            return ResponseEntity.badRequest().body(errors.toString());
        }

        appointmentRulesService.register(dto);
        return ResponseEntity.ok("Cadastro realizado com sucesso");
    }

    @GetMapping("/canberescheduled/{clientId}/{appointmentId}")
    public ResponseEntity<CanRescheduleDTO> canAppointmentBeRescheduled(
        @PathVariable Long clientId, @PathVariable Long appointmentId) {
        CanRescheduleDTO responseDTO = appointmentRulesService.canAppointmedBeRescheduled(appointmentId, clientId);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/canreschedule/{clientId}")
    public ResponseEntity<CanRescheduleDTO> canClientReschedule(@PathVariable Long clientId) {
        CanRescheduleDTO responseDTO = appointmentRulesService.canClientReschedule(clientId);
        return ResponseEntity.ok(responseDTO);
    }
}
