package com.horafit.backend.service;

import com.horafit.backend.dto.appointmentRules.CanRescheduleDTO;
import com.horafit.backend.entity.Appointment;
import com.horafit.backend.entity.AppointmentClient;
import com.horafit.backend.entity.enums.AppointmentConfirmation;
import com.horafit.backend.repository.AppointmentClientRepository;
import com.horafit.backend.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.horafit.backend.dto.appointmentRules.AppointmentRulesDTO;
import com.horafit.backend.entity.AppointmentRules;
import com.horafit.backend.entity.Client;
import com.horafit.backend.repository.AppointmentRulesRepository;
import com.horafit.backend.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
public class AppointmentRulesService {
  @Autowired
  AppointmentRepository appointmentRepository;

  @Autowired
  AppointmentRulesRepository appointmentRulesRepository;

  @Autowired
  ClientRepository clientRepository;

  @Autowired
  AppointmentClientRepository appointmentClientRepository;

  public AppointmentRules register(AppointmentRulesDTO obj) {
    AppointmentRules rule = new AppointmentRules();

    rule.setName(obj.getName());
    rule.setReeschedulingLimit(obj.getReeschedulingLimit());
    rule.setReeschedulingMinHoursInAdvance(obj.getReeschedulingMinHoursInAdvance());
    rule.setMaxClientsPerGroup(obj.getMaxClientsPerGroup());
    rule.setFrequency(obj.getFrequency());

    return appointmentRulesRepository.save(rule);
  }

  public AppointmentRules findById(Long id) {
    return appointmentRulesRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Regra de agendamento não encontrado para o id: " + id));
  }

  public CanRescheduleDTO canAppointmedBeRescheduled(Long appointmentId, Long clientId) {
    Appointment appointment = appointmentRepository.findById(appointmentId)
        .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

    Client client = clientRepository.findById(clientId)
        .orElseThrow(() -> new EntityNotFoundException("Client not found"));

    // verifica quantas horas até o atendimento, compara com o número
    // mínimo de horas de antecedência para o cancelamento poder ser remarcado
    Long hoursUntilAppointment = calculateHoursUntilAppointments(appointment);

    // retorna dto com false caso esse limite seja inferior
    if (hoursUntilAppointment
        <= client.getAppointmentRules().getReeschedulingMinHoursInAdvance()) {
      return new CanRescheduleDTO(
          false,
          "min_rescheduling_hours_in_advance_reached",
          "Este a tendimento ocorrerá em menos de " +
              client.getAppointmentRules().getReeschedulingMinHoursInAdvance() + " horas. " +
              "Ao realizar o cancelamento, você não terá direito a remarcação."
      );
    }
    Integer rescheduledAppointmentsInMonth = appointmentRulesRepository.countRescheduledAppointmentsInMonth(client.getId());

    if (rescheduledAppointmentsInMonth == null) rescheduledAppointmentsInMonth = 0;

    // compara a quantidade de atendimentos remarcados pelo cliente no mês
    // com o limite de remarcações no mês (regra de negócio)
    // e retorna dto com false caso cliente já tenha atingido o limite de remarcações
    if (rescheduledAppointmentsInMonth
        >= client.getAppointmentRules().getReeschedulingLimit()) {
      return new CanRescheduleDTO(
          false,
          "max_reschedule_limit_reached",
          "São permitidas apenas 2 remarcações por mês. Você já realizou " +
              appointmentRulesRepository.countRescheduledAppointmentsInMonth(clientId) +
              " remarcação(es) em " + getCurrentMonth() + ". " +
              "Ao realizar o cancelamento, você não terá direito a remarcação."
      );
    }

    return new CanRescheduleDTO(
        true,
        "client_can_reschedule",
        "São permitidas apenas 2 remarcações por mês. Você já realizou " +
            rescheduledAppointmentsInMonth +
            " remarcação(es) em " + getCurrentMonth() + "."
    );
  }

  public CanRescheduleDTO canClientReschedule(Long clientId) {
    List<AppointmentClient> appointmentClients = appointmentClientRepository.findByClientIdInCurrentAndNextMonth(clientId);
    int countRescheduled = 0;
    int countCanceledWithRescheduling = 0;

    for (AppointmentClient ac : appointmentClients) {
      if (ac.getConfirmation().equals(AppointmentConfirmation.RESCHEDULED)) {
        countRescheduled++;
      } else if (ac.getConfirmation().equals(AppointmentConfirmation.CANCELED_WITH_RESCHEDULING)) {
        countCanceledWithRescheduling++;
      }
    }

    if (countRescheduled >= 2) {
      return new CanRescheduleDTO(
          false,
          "max_reschedule_limit_reached",
          "São permitidas apenas 2 remarcações por mês. Você já realizou " +
              appointmentRulesRepository.countRescheduledAppointmentsInMonth(clientId) +
              " remarcação(es) em " + getCurrentMonth() + ". " +
              "Ao realizar o cancelamento, você não terá direito a remarcação."
      );
    }

    if (countCanceledWithRescheduling <= 0) {
      return new CanRescheduleDTO(
          false,
          "no_appointment_to_reschedule",
          "Para fazer a remarcação, primeiro cancele o atendimento desejado na aba \"Meus atendimentos\"."
      );
    }

    return new CanRescheduleDTO(
        true,
        "client_can_reschedule",
        "Client can reschedule"
    );
  }

  public Long calculateHoursUntilAppointments(Appointment appointment) {
    LocalDateTime currentDateTime = LocalDateTime.now();
    return ChronoUnit.HOURS.between(currentDateTime, appointment.getDateTime());
  }

  private String getCurrentMonth() {
    return LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
  }

}
