package com.horafit.backend.controller;

import com.horafit.backend.dto.client.ClientDTO;
import com.horafit.backend.dto.client.ClientRegisterDTO;
import com.horafit.backend.dto.client.ResetPasswordDTO;
import com.horafit.backend.dto.appointmentRules.UpdateAppointmentRulesDTO;
import com.horafit.backend.entity.BusinessRules;
import com.horafit.backend.entity.Client;
import com.horafit.backend.service.ClientService;
import com.horafit.backend.util.exception.client.ClientException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/client")
@CrossOrigin
@Tag(name = "Client API", description = "API para operações relacionadas aos clientes.")
public class ClientController {
    @Autowired
    private ClientService clientService;

    @Operation(summary = "Cadastrar novo cliente.", description = "Este endpoint cadastra um novo cliente no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cadastro realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody ClientRegisterDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            result.getAllErrors().forEach(error -> errors.append(error.getDefaultMessage()).append(". "));
            return ResponseEntity.badRequest().body(errors.toString());
        }

        clientService.register(dto);
        return ResponseEntity.ok("Cadastro realizado com sucesso");
    }


    @Operation(summary = "Resetar senha de cliente.", description = "Reseta a senha de um cliente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha resetada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao resetar senha", content = @Content)
    })
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            result.getAllErrors().forEach(error -> errors.append(error.getDefaultMessage()).append(". "));
            return ResponseEntity.badRequest().body(errors.toString());
        }

        clientService.resetPassword(dto);
        return ResponseEntity.ok("Senha resetada com sucesso");
    }


    @Operation(summary = "Aceitar contrato do cliente.", description = "Atualiza o campo signed_contract para indicar que o cliente aceitou o contrato.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contrato aceito com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    @PutMapping("/{id}/accept-contract")
    public ResponseEntity<String> acceptContract(@PathVariable Long id) {
        try {
            clientService.acceptContract(id);
            return ResponseEntity.ok("Contrato aceito com sucesso");
        } catch (ClientException.ClientNotFoundException | ClientException.ContractAlreadySigned e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @Operation(summary = "Obter todos os clientes.", description = "Retorna uma lista de todos os clientes cadastrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Client.class)))
    })
    @GetMapping("/all")
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        List<Client> clients = clientService.getAllClients();

        List<ClientDTO> clientDTOs = clients.stream()
                .map(client -> new ClientDTO(client.getId(), client.getName(), client.getEmail(), client.getSignedContract()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(clientDTOs);
    }


    @Operation(summary = "Atualizar regras de agendamento do cliente", description = "Atualiza o campo appointment_rules.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regras de agendamento atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    @PutMapping("/update-appointment_rules")
    public ResponseEntity<String> updateClientAppointmentRules(@RequestBody UpdateAppointmentRulesDTO dto) {
        try {
            clientService.updateClientAppointmentRules(dto.getEmail(), dto.getIdAppointmentRules());
            return ResponseEntity.ok("Regra de agendamento do cliente atualizado com sucesso");
        }catch (ClientException.ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente não encontrado: " + e.getMessage());
        }catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro inesperado: " + e.getMessage());
        }
    }
    

    @Operation(summary = "Visualizar regras de serviço.", description = "Cliente visualiza regras de serviço em caso de primeiro acesso ou até o aceite das regras")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regras de serviço retornadas com sucesso"),
            @ApiResponse(responseCode = "204", description = "Regras de serviço já visualizadas pelo cliente"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    @GetMapping("/viewed-rules")
    public ResponseEntity<List<BusinessRules>> viewedServiceRules() {
        return ResponseEntity.ok(clientService.showBusinessRules());
    }


    @Operation(summary = "Visualizar status do contrato.", description = "Retorna falso caso o cliente ainda não tenha assinado o contrato")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content)
    })
    @GetMapping("{id}/status-contrato")
    public ResponseEntity<Boolean> contractStatus(@PathVariable Long id) {

        boolean contratoAceito = clientService.contractStatus(id);
         return ResponseEntity.ok(contratoAceito);
    }
}
