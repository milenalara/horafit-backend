package com.horafit.backend.controller;

import com.horafit.backend.dto.appointment.AddClientToAppointmentDTO;
import com.horafit.backend.dto.appointment.AppointmentCreateDTO;
import com.horafit.backend.dto.response.ResponseDTO;
import com.horafit.backend.dto.appointment.AppointmentUpdateDTO;
import com.horafit.backend.dto.businessRules.BusinessRulesRegisterDTO;
import com.horafit.backend.dto.client.ClientDTO;
import com.horafit.backend.entity.Client;
import com.horafit.backend.service.BusinessRulesService;
import com.horafit.backend.service.PhysiotherapistService;
import com.horafit.backend.util.exception.appointment.AppointmentsException;
import com.horafit.backend.util.exception.appointment.AppointmentsException.*;
import com.horafit.backend.util.exception.client.ClientException;
import com.horafit.backend.util.exception.physiotherapist.PhysiotherapistException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/physiotherapist")
@CrossOrigin
@Tag(name = "Physiotherapist API", description = "API para operações relacionadas aos procedimentos do fisioterapeuta.")
public class PhysiotherapistController {
    @Autowired
    PhysiotherapistService physiotherapistService;

    @Autowired
    private BusinessRulesService businessRulesService;

    @Operation(summary = "Criar um novo atendimento.",
            description = "Este endpoint permite que o fisioterapeuta possa criar um atendimento para um ou mais clientes. " +
                    "Verifica se já existe um atendimento agendado para outro cliente no mesmo dia e horário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atendimento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Já existe um atendimento agendado para outro cliente neste dia e horário"),
    })
    @PostMapping("/create/appointment")
    public ResponseEntity<ResponseDTO> createAppointmentClient(@RequestBody AppointmentCreateDTO appointmentDTO) {
        try {
            physiotherapistService.createAppointmentClient(appointmentDTO);
            ResponseDTO response = new ResponseDTO("Atendimento criado com sucesso", 200);
            return ResponseEntity.ok(response);
        } catch (AppointmentAlreadyExistsException | IllegalArgumentException e) {
            ResponseDTO response = new ResponseDTO("Erro: " + e.getMessage(), 400);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            ResponseDTO response = new ResponseDTO("Erro inesperado: " + e.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Atualizar um atendimento existente.",
    description = "Permite atualizar a data e o horário de um atendimento. "
    + "Verifica se já existe um atendimento agendado para o mesmo fisioterapeuta e horário.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Atendimento atualizado com sucesso."),
        @ApiResponse(responseCode = "400", description = "Erro na requisição. Verifique os dados enviados."),
    })
    @PutMapping("/edit/appointment")
    public ResponseEntity<ResponseDTO> updateAppointment(@RequestBody AppointmentUpdateDTO appointmentUpdateDTO) {
        try {
            physiotherapistService.updateAppointment(appointmentUpdateDTO);
            ResponseDTO response = new ResponseDTO("Atendimento atualizado com sucesso", 200);
            return ResponseEntity.ok(response);
        } catch (AppointmentAlreadyExistsException | IllegalArgumentException e) {
            ResponseDTO response = new ResponseDTO("Erro: " + e.getMessage(), 400);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            ResponseDTO response = new ResponseDTO("Erro inesperado: " + e.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Adicionar um ou mais clientes a um atendimento existente.",
            description = "Permite adicionar um ou mais clientes a um atendimento já criado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clientes adicionados com sucesso ao atendimento"),
            @ApiResponse(responseCode = "404", description = "Atendimento ou cliente não encontrado"),
            @ApiResponse(responseCode = "400", description = "Cliente já associado a este atendimento"),
    })
    @PostMapping("/appointment/add-clients-to-empty")
    public ResponseEntity<ResponseDTO> addClientsToAppointment(@RequestBody AddClientToAppointmentDTO dto) {
        try {
            physiotherapistService.addClientsToAppointment(dto.getAppointmentId(), dto.getClientIds());
            ResponseDTO response = new ResponseDTO("Cliente(s) adicionados com sucesso ao atendimento", 200);
            return ResponseEntity.ok(response);
        } catch (AppointmentsException.AppointmentNotFoundException | ClientException.ClientNotFoundException e) {
            ResponseDTO response = new ResponseDTO("Erro: " + e.getMessage(), 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (ClientException.ClientAlreadyExistsException e) {
            ResponseDTO response = new ResponseDTO("Erro: " + e.getMessage(), 400);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            ResponseDTO response = new ResponseDTO("Erro inesperado: " + e.getMessage(), 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Deletar cliente de um atendimento.",
            description = "Este endpoint permite o fisioterapeuta deletar um cliente de um atendimento com base no ID do antedimento e no id do cliente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Atendimento deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Atendimento não encontrado")
    })
    @DeleteMapping("/delete/{clientId}/{appointmentId}")
    public ResponseEntity<ResponseDTO> deleteClientFromAppointment(@PathVariable Long clientId, @PathVariable Long appointmentId) {
        try {
            physiotherapistService.deleteClientFromAppointment(clientId, appointmentId);
            ResponseDTO response = new ResponseDTO("Atendimento deletado com sucesso", 200);
            return ResponseEntity.ok(response);
        } catch (AppointmentsException.AppointmentDeleteException e) {
            ResponseDTO response = new ResponseDTO("Erro: " + e.getMessage(), 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }


    @Operation(summary = "Obter clientes pelo nome.", description = "Retorna uma lista de clientes que contêm o nome fornecido.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de clientes retornada com sucesso",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientDTO.class))),
        @ApiResponse(responseCode = "404", description = "Nenhum cliente encontrado com o nome fornecido",
        content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<List<ClientDTO>> getClientsByName(@RequestParam String name) {
        List<Client> clients = physiotherapistService.findClientsByName(name);

        if (clients.isEmpty()) {
            throw new PhysiotherapistException.ClientNotFound("Nenhum cliente encontrado com o nome fornecido.");
        }

        List<ClientDTO> clientDTOs = clients.stream()
                .map(client -> new ClientDTO(client.getId(), client.getName(), client.getEmail(), client.getSignedContract()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(clientDTOs);
    }

    @Operation(summary = "Criar uma nova regra de serviço.",
        description = "Este endpoint permite que o fisioterapeuta possa criar regras de remarcações editáveis. " )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Regras de serviço criadas com sucesso"),
    })
    @PostMapping("/create/business-rules")
    public ResponseEntity<String> createBusinessRules(@RequestBody BusinessRulesRegisterDTO dto) {
    try {
        businessRulesService.register(dto);
        return ResponseEntity.ok("Regras de serviços criadas com sucesso");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro inesperado: " + e.getMessage());
    }
  }
}
