package com.horafit.backend.controller;

import com.horafit.backend.dto.appointment.AppointmentEditBatchDTO;
import com.horafit.backend.dto.appointment.AppointmentEditDTO;
import com.horafit.backend.dto.appointment.AppointmentFilterDTO;
import com.horafit.backend.dto.appointment.AppointmentGetDTO;
import com.horafit.backend.dto.client.ClientScheduleDTO;
import com.horafit.backend.dto.client.ClientSimpleDTO;
import com.horafit.backend.entity.Appointment;
import com.horafit.backend.service.AppointmentService;
import com.horafit.backend.util.exception.appointment.AppointmentsException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/appointment")
@CrossOrigin
@Tag(name = "Appointment API", description = "API para operações relacionadas aos atendimentos de clientes.")
public class AppointmentController {
  @Autowired
  AppointmentService appointmentService;

  @Operation(summary = "Obter todos os atendimentos.", description = "Este endpoint retorna uma lista de todos os atendimentos agendados.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista de atendimentos retornada com sucesso")
  })
  @GetMapping("/all")
  public ResponseEntity<List<Appointment>> getAllAppointments() {
    List<Appointment> appointments = appointmentService.findAll();
    return ResponseEntity.ok(appointments);
  }

  @Operation(summary = "Obter atendimentos por cliente.", description = "Este endpoint retorna uma lista de atendimentos agendados para um cliente específico.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista de atendimentos do cliente retornada com sucesso"),
      @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
  })
  @GetMapping("/client/{id}")
  public ResponseEntity<List<AppointmentGetDTO>> getAppointmentsByClient(@PathVariable Long id) {
    List<AppointmentGetDTO> appointments = appointmentService.getAllByClientId(id);
    return ResponseEntity.ok(appointments);
  }

  @Operation(summary = "Obter atendimentos futuros do cliente.", description = "Retorna uma lista de atendimentos de um cliente agendados a partir da data e hora atuais")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista de atendimentos futuros do cliente retornada com sucesso"),
      @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
  })
  @GetMapping("/future/client/{id}")
  public ResponseEntity<List<AppointmentGetDTO>> getFutureAppointmentsByClient(@PathVariable Long id) {
    List<AppointmentGetDTO> appointments = appointmentService.getFutureAppointmentsByClientId(id);
    return ResponseEntity.ok(appointments);
  }

  @Operation(summary = "Obter atendimentos por fisioterapeuta.", description = "Este endpoint retorna uma lista de atendimentos agendados para um fisioterapeuta específico.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista de atendimentos do fisioterapeuta retornada com sucesso"),
      @ApiResponse(responseCode = "404", description = "Fisioterapeuta não encontrado")
  })
  @GetMapping("/physio/{id}")
  public ResponseEntity<List<AppointmentGetDTO>> getAppointmentsByPhysiotherapist(@PathVariable Long id) {
    List<AppointmentGetDTO> appointments = appointmentService.findAllByPhysiotherapistId(id);
    return ResponseEntity.ok(appointments);
  }

  @Operation(summary = "Obter atendimentos por data.", description = "Este endpoint retorna uma lista de atendimentos agendados para uma data específica.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista de atendimentos na data especificada retornada com sucesso"),
      @ApiResponse(responseCode = "400", description = "Data inválida")
  })
  @GetMapping("/date/{date}")
  public ResponseEntity<List<AppointmentGetDTO>> getAppointmentsByDate(@PathVariable String date) {
    List<AppointmentGetDTO> appointments = appointmentService.findAllByDate(date);
    return ResponseEntity.ok(appointments);
  }

  @Operation(summary = "Obter atendimentos disponíveis.", description = "Este endpoint retorna uma lista de atendimentos vagos.")
  @GetMapping("/available/{clientId}")
  public ResponseEntity<List<AppointmentGetDTO>> getAvailable(@PathVariable Long clientId) {
    List<AppointmentGetDTO> availableAppointments = appointmentService.findAvailable(clientId);
    return ResponseEntity.ok(availableAppointments);
  }

  @Operation(summary = "Cancelar atendimento.", description = "Este endpoint permite que o cliente cancele um atendimento agendado.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Atendimento cancelado com sucesso"),
      @ApiResponse(responseCode = "404", description = "Atendimento não encontrado")
  })
  @PutMapping("/cancel/{clientId}/{appointmentId}")
  public ResponseEntity<Void> cancelAppointment(@PathVariable Long clientId, @PathVariable Long appointmentId) {
    appointmentService.cancelAppointment(clientId, appointmentId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Reagendar atendimento.", description = "Este endpoint permite reagendar um atendimento após ter sido feito o cancelamento.")
  @PutMapping("/reschedule/{clientId}/{appointmentId}")
  public ResponseEntity<Void> rescheduleAppointment(@PathVariable Long clientId, @PathVariable Long appointmentId) {
    appointmentService.rescheduleAppointment(clientId, appointmentId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Editar um atendimento específico", description = "Permite editar o atendimento específico de um cliente, alterando data e hora. Este endpoint será utilizado pelo fisioterapeuta")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Atendimento deletado com sucesso"),
      @ApiResponse(responseCode = "404", description = "Atendimento não encontrado")
  })
  @PutMapping("/edit/{clientId}/{appointmentId}")
  public ResponseEntity<Void> editIndividualAppointment(@RequestBody AppointmentEditDTO obj,
      @PathVariable Long clientId, @PathVariable Long appointmentId) {

    appointmentService.updateAppointmentClient(obj, clientId, appointmentId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Alterar plano de atendimentos em batch.", description = "Permite alterar o plano de atendimentos de um cliente, excluindo os já existentes e criando novos nos dias e modalidade selecionados. Este endpoint será utilizado pelo fisioterapeuta")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Atendimentos atualizados com sucesso"),
      @ApiResponse(responseCode = "400", description = "Requisição inválida, erro nos dados fornecidos"),
      @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
  })
  @PutMapping("/batch-edit/{clientId}")
  public ResponseEntity<Void> editBatchAppointments(@RequestBody AppointmentEditBatchDTO obj,
      @PathVariable Long clientId) {

    appointmentService.updateBatchAppointmentsForClient(obj, clientId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Filtrar atendimentos de cliente.", description = "Este endpoint retorna uma lista de atendimentos filtrados por cliente, modalidade e data.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista de atendimentos filtrados retornada com sucesso"),
      @ApiResponse(responseCode = "404", description = "Nenhum atendimento encontrado")
  })
  @PostMapping("/filter")
  public ResponseEntity<List<AppointmentGetDTO>> filterAppointments(@RequestBody AppointmentFilterDTO filterDTO) {
    if (filterDTO.getClientId() == null) {
      throw new AppointmentsException.AppointmentNotFoundException("O ID do cliente deve ser fornecido.");
    }

    List<AppointmentGetDTO> appointments = appointmentService.filterAppointments(filterDTO);

    if (appointments.isEmpty()) {
      throw new AppointmentsException.AppointmentNotFoundException(
          "Nenhum atendimento encontrado para os filtros aplicados ao cliente com ID: " + filterDTO.getClientId());
    }
    return ResponseEntity.ok(appointments);
  }

  @Operation(summary = "Obter clientes com atendimentos.", description = "Este endpoint retorna uma lista de clientes que possuem atendimentos agendados.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista de clientes obtida com sucesso"),
      @ApiResponse(responseCode = "404", description = "Nenhum cliente encontrado")
  })
  @GetMapping("/clients")
  public ResponseEntity<List<ClientSimpleDTO>> getClientsWithAppointments() {
    List<ClientSimpleDTO> clients = appointmentService.getClientsWithAppointments();
    return ResponseEntity.ok(clients);
  }

  @Operation(summary = "Obter informações detalhadas de um cliente específico.", description = "Este endpoint retorna todas as informações detalhadas de um cliente específico, incluindo nome, modalidade, frequência, dias e horários de atendimento, aceite do contrato e último pagamento.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Informações do cliente obtidas com sucesso."),
      @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
  })
  @GetMapping("/client/{clientId}/detalhes")
  public ResponseEntity<ClientScheduleDTO> getClientById(@PathVariable Long clientId) {
    ClientScheduleDTO clientSchedule = appointmentService.getClientSchedule(clientId);
    return ResponseEntity.ok(clientSchedule);
  }
}
