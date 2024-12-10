package com.horafit.backend.service;

import com.horafit.backend.dto.appointment.*;
import com.horafit.backend.dto.appointmentRules.AppointmentRulesScheduleDTO;
import com.horafit.backend.dto.appointmentRules.CanRescheduleDTO;
import com.horafit.backend.dto.client.ClientScheduleDTO;
import com.horafit.backend.dto.client.ClientSimpleDTO;
import com.horafit.backend.entity.*;
import com.horafit.backend.entity.enums.AppointmentConfirmation;
import com.horafit.backend.repository.*;
import com.horafit.backend.util.exception.appointment.AppointmentsException;
import com.horafit.backend.util.exception.client.ClientException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppointmentService {
  @Autowired
  private AppointmentRepository appointmentRepository;

  @Autowired
  private AppointmentClientRepository appointmentClientRepository;

  @Autowired
  private ClientRepository clientRepository;

  @Autowired
  private PhysiotherapistRepository physiotherapistRepository;

  @Autowired
  private PaymentService paymentService;

  @Autowired
  private AppointmentRulesRepository appointmentRulesRepository;

  @Autowired
  private PhysiotherapistService physiotherapistService;

  /**
   * Retrieves all appointments from the repository.
   *
   * @return a list of {@link Appointment} entities representing all stored
   * appointments.
   */
  public List<Appointment> findAll() {
    return appointmentRepository.findAll();
  }

  /**
   * Retrieves an appointment by its ID.
   *
   * @param id the ID of the appointment to retrieve.
   * @return the {@link Appointment} entity corresponding to the specified ID.
   */
  public Appointment findById(Long id) {
    return appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Client not found"));
  }

  /**
   * Retrieves all clients who have at least one associated appointment.
   *
   * @return a list of {@link ClientSimpleDTO} objects representing clients with
   * appointments,
   * each containing the client's ID and name.
   */
  public List<ClientSimpleDTO> getClientsWithAppointments() {
    List<Client> clients = clientRepository.findClientsWithAppointments();

    return clients.stream()
        .map(client -> new ClientSimpleDTO(client.getId(), client.getName()))
        .collect(Collectors.toList());
  }

  /**
   * Retrieves the complete schedule of a specific client, including personal
   * details, contract status,
   * payment history, appointment rules, and grouped appointments by modality and
   * days of the week.
   *
   * @param clientId the ID of the client whose schedule is to be retrieved.
   * @return a {@link ClientScheduleDTO} object containing detailed information
   * about the client's schedule,
   * including grouped appointments by modality and days of the week.
   */
  public ClientScheduleDTO getClientSchedule(Long clientId) {
    Client client = clientRepository.findById(clientId)
        .orElseThrow(() -> new ClientException.ClientNotFoundException("Cliente não encontrado"));

    List<Appointment> appointments = appointmentRepository.findAppointmentsByClientId(clientId);

    ClientScheduleDTO clientScheduleDTO = new ClientScheduleDTO();
    clientScheduleDTO.setNome(client.getName());
    clientScheduleDTO.setAceiteContrato(client.getSignedContract().toString());
    clientScheduleDTO.setUltimoPagamento(paymentService.getUltimoPagamento(clientId));

    AppointmentRules appointmentRules = client.getAppointmentRules();
    if (appointmentRules != null) {
      AppointmentRulesScheduleDTO appointmentRulesScheduleDTO = new AppointmentRulesScheduleDTO();
      appointmentRulesScheduleDTO.setId(appointmentRules.getId());
      appointmentRulesScheduleDTO.setName(appointmentRules.getName());
      appointmentRulesScheduleDTO.setFrequency(String.valueOf(appointmentRules.getFrequency()));

      clientScheduleDTO.setRegrasDeRemarcacao(appointmentRulesScheduleDTO);
    } else {
      System.out.println("Não há regras de remarcação associadas a este cliente.");
    }

    Map<String, List<Appointment>> appointmentsByModality = appointments.stream()
        .collect(Collectors.groupingBy(appointment -> appointment.getModality().name()));

    List<AppointmentDTO> modalityDTOs = new ArrayList<>();

    for (Map.Entry<String, List<Appointment>> entry : appointmentsByModality.entrySet()) {
      String modality = entry.getKey();
      List<Appointment> modalityAppointments = entry.getValue();

      AppointmentDTO modalityDTO = new AppointmentDTO();
      modalityDTO.setModalidade(modality);

      Map<String, List<LocalTime>> scheduleByDay = modalityAppointments.stream()
          .collect(Collectors.groupingBy(
              a -> a.getDateTime().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()),
              Collectors.mapping(a -> a.getDateTime().toLocalTime(), Collectors.toList())));

      List<DayScheduleDTO> scheduleTimeDTOs = new ArrayList<>();

      for (Map.Entry<String, List<LocalTime>> scheduleEntry : scheduleByDay.entrySet()) {
        DayScheduleDTO scheduleTimeDTO = new DayScheduleDTO();
        scheduleTimeDTO.setDia(scheduleEntry.getKey());
        scheduleTimeDTO.setHorarios(scheduleEntry.getValue());
        scheduleTimeDTOs.add(scheduleTimeDTO);
      }

      modalityDTO.setDiasDaSemana(scheduleTimeDTOs);
      modalityDTOs.add(modalityDTO);
    }

    clientScheduleDTO.setAtendimentos(modalityDTOs);

    return clientScheduleDTO;
  }

  /**
   * Retrieves all appointments associated with a specific client.
   * For each appointment, it filters the list of associated clients to include
   * only the specified client,
   * ensuring that the returned data is client-specific.
   *
   * @param clientId the ID of the client whose appointments are to be retrieved.
   * @return a list of {@link AppointmentGetDTO} objects representing all
   * appointments
   * associated with the specified client.
   */
  public List<AppointmentGetDTO> getAllByClientId(Long clientId) {
    List<AppointmentClient> appointmentClients = appointmentClientRepository.findByClient_Id(clientId);

    return appointmentClients.stream()
        .map(AppointmentClient::getAppointment)
        .distinct()
        .map(appointment -> {
          List<AppointmentClient> filteredClients = appointmentClientRepository.findByAppointment(appointment).stream()
              .filter(appointmentClient -> appointmentClient.getClient().getId().equals(clientId))
              .collect(Collectors.toList());

          return convertToGetDTO(appointment, filteredClients);
        })
        .collect(Collectors.toList());
  }

  /**
   * Retrieves a list of future appointments for a specific client.
   * Filters the appointments based on the client's ID and ensures that only
   * upcoming appointments
   * are included in the result.
   *
   * @param clientId the ID of the client whose future appointments are to be
   *                 retrieved.
   * @return a list of {@link AppointmentGetDTO} objects representing the future
   * appointments
   * associated with the specified client.
   */
  public List<AppointmentGetDTO> getFutureAppointmentsByClientId(Long clientId) {
    List<AppointmentClient> appointmentClients = appointmentClientRepository.findFutureAppointmentsByClientId(clientId);

    return appointmentClients.stream()
        .map(AppointmentClient::getAppointment)
        .distinct()
        .map(this::convertToGetDTO)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves all appointments associated with a specific physiotherapist.
   *
   * @param id the ID of the physiotherapist whose appointments are to be
   *           retrieved.
   * @return a list of {@link AppointmentGetDTO} objects representing the
   * appointments
   * assigned to the specified physiotherapist.
   */
  public List<AppointmentGetDTO> findAllByPhysiotherapistId(Long id) {
    List<Appointment> appointments = appointmentRepository.findByPhysiotherapist_Id(id);
    return appointments.stream().map(this::convertToGetDTO).collect(Collectors.toList());
  }

  /**
   * Retrieves all appointments scheduled on a specific date.
   *
   * @param strDate the date in string format (e.g., "YYYY-MM-DD") for which
   *                appointments are to be retrieved.
   * @return a list of {@link AppointmentGetDTO} objects representing the
   * appointments scheduled on the given date.
   */
  public List<AppointmentGetDTO> findAllByDate(String strDate) {
    LocalDate date = LocalDate.parse(strDate);
    List<Appointment> appointments = appointmentRepository.findByDate(date);
    return appointments.stream().map(this::convertToGetDTO).collect(Collectors.toList());
  }

  /**
   * Retrieves all available appointments for a specific client.
   * Filters appointments based on availability and excludes appointments already
   * associated with the client.
   *
   * @param clientId the ID of the client for whom available appointments are to
   *                 be retrieved.
   * @return a list of {@link AppointmentGetDTO} objects representing available
   * appointments
   * for the specified client.
   */
  public List<AppointmentGetDTO> findAvailable(Long clientId) {
    List<Appointment> availableAppointments = appointmentRepository.findAvailable(clientId);
    return availableAppointments.stream().map(this::convertToGetDTO).collect(Collectors.toList());
  }

  /**
   * Filters appointments for a client based on the provided filter criteria.
   * Supports filtering by modality, month/year, or a combination of both. If no
   * filters are provided,
   * retrieves all appointments for the specified client.
   *
   * @param filterDTO the {@link AppointmentFilterDTO} containing the filter
   *                  criteria
   * @return a list of {@link AppointmentGetDTO} objects representing the filtered
   * appointments.
   * If no filters are applied, returns all appointments for the specified
   * client.
   */
  public List<AppointmentGetDTO> filterAppointments(AppointmentFilterDTO filterDTO) {
    Long clientId = filterDTO.getClientId();
    List<Appointment> appointments;

    if (filterDTO.getModality() != null && !filterDTO.getModality().isEmpty() &&
        filterDTO.getMonthYear() != null && !filterDTO.getMonthYear().isEmpty()) {
      appointments = appointmentRepository.findByClientIdAndModalityAndMonthYear(clientId, filterDTO.getModality(),
          filterDTO.getMonthYear());
    } else if (filterDTO.getModality() != null && !filterDTO.getModality().isEmpty()) {
      appointments = appointmentRepository.findByClientIdAndModality(clientId, filterDTO.getModality());
    } else if (filterDTO.getMonthYear() != null && !filterDTO.getMonthYear().isEmpty()) {
      appointments = appointmentRepository.findByClientIdAndMonthYear(clientId, filterDTO.getMonthYear());
    } else {
      appointments = appointmentRepository.findByClientId(clientId);
    }

    return appointments.stream()
        .map(this::convertToGetDTO)
        .collect(Collectors.toList());
  }

  /**
   * Cancels an appointment for a client and determines whether it can be
   * rescheduled
   * based on the time remaining until the appointment and predefined cancellation
   * rules.
   *
   * @param clientId      the ID of the client requesting the cancellation.
   * @param appointmentId the ID of the appointment to be canceled.
   * @return the updated {@link AppointmentClient} entity after cancellation.
   * @Transactional This method ensures the cancellation and updates are executed
   * within a transactional context.
   */
  @Transactional
  public AppointmentClient cancelAppointment(Long clientId, Long appointmentId) {
    Appointment appointment = appointmentRepository.findById(appointmentId)
        .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

    Client client = clientRepository.findById(clientId)
        .orElseThrow(() -> new EntityNotFoundException("Client not found"));

    AppointmentClient appointmentClient = appointmentClientRepository
        .findAppointmentClientByClient_IdAndAppointment_Id(clientId, appointmentId)
        .orElseThrow(() -> new AppointmentsException.AppointmentNotFoundException(
            "Appointment not found for client with id = " + clientId));

    Long hoursUntilAppointment = calculateHoursUntilAppointments(appointment);

    if (hoursUntilAppointment
        <= client.getAppointmentRules().getReeschedulingMinHoursInAdvance()) {
      appointmentClient.setConfirmation(AppointmentConfirmation.CANCELED_WITHOUT_RESCHEDULING);
      return appointmentClientRepository.save(appointmentClient);
    }

    Integer rescheduledAppointmentsInMonth = appointmentRulesRepository.countRescheduledAppointmentsInMonth(client.getId());
    if (rescheduledAppointmentsInMonth == null) rescheduledAppointmentsInMonth = 0;

    if (rescheduledAppointmentsInMonth
        >= client.getAppointmentRules().getReeschedulingLimit()) {
      appointmentClient.setConfirmation(AppointmentConfirmation.CANCELED_WITHOUT_RESCHEDULING);
      return appointmentClientRepository.save(appointmentClient);
    }

    appointmentClient.setConfirmation(AppointmentConfirmation.CANCELED_WITH_RESCHEDULING);
    return appointmentClientRepository.save(appointmentClient);
  }

  /**
   * Reschedules an appointment for a client by creating a new appointment-client
   * association.
   * Validates that the appointment and client exist, and ensures that the client
   * is not already
   * scheduled for the specified appointment.
   *
   * @param clientId      the ID of the client to reschedule the appointment for.
   * @param appointmentId the ID of the appointment to reschedule.
   * @return the newly created {@link AppointmentClient} entity after
   * rescheduling.
   * @Transactional This method runs in a transactional context to ensure
   * atomicity of database operations.
   */
  @Transactional
  public AppointmentClient rescheduleAppointment(Long clientId, Long appointmentId) {
    Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(
        () -> new RuntimeException("Appointment not found"));

    Client client = clientRepository.findById(clientId).orElseThrow(
        () -> new RuntimeException("Client not found"));

    Optional<AppointmentClient> existingAppointmentClient = appointmentClientRepository
        .findAppointmentClientByClient_IdAndAppointment_Id(clientId, appointmentId);

    if (existingAppointmentClient.isPresent()) {
      throw new RuntimeException("O cliente já está agendado para este atendimento");
    }

    AppointmentClient newAppointmentClient = new AppointmentClient();
    newAppointmentClient.setAppointment(appointment);
    newAppointmentClient.setClient(client);
    newAppointmentClient.setConfirmation(AppointmentConfirmation.RESCHEDULED);
    newAppointmentClient.setAttendance(true);

    return appointmentClientRepository.save(newAppointmentClient);
  }

  /**
   * Updates an appointment for a client, reassigning it to a new date and
   * physiotherapist.
   * Validates that the client and appointment exist, the new physiotherapist and
   * date are available,
   * and that the appointment does not exceed the maximum number of clients
   * allowed.
   *
   * @param appointmentEditDTO the data transfer object containing the new
   *                           appointment details
   *                           (physiotherapist ID and date/time).
   * @param clientId           the ID of the client whose appointment is being
   *                           updated.
   * @param appointmentId      the ID of the current appointment to be updated.
   * @return the updated {@link AppointmentClient} entity after reassignment.
   * @Transactional This method runs in a transactional context to ensure
   * atomicity of updates.
   */
  @Transactional
  public AppointmentClient updateAppointmentClient(AppointmentEditDTO appointmentEditDTO, Long clientId,
                                                   Long appointmentId) {
    AppointmentClient appointmentClient = appointmentClientRepository
        .findAppointmentClientByClient_IdAndAppointment_Id(clientId, appointmentId)
        .orElseThrow(() -> new AppointmentsException.AppointmentNotFoundException(
            "Appointment not found for client with id = " + clientId));

    if (appointmentClient == null) {
      throw new RuntimeException("O cliente não está agendado para este atendimento");
    }

    clientRepository.findById(clientId)
        .orElseThrow(() -> new RuntimeException("Client not found"));

    Physiotherapist physiotherapist = physiotherapistRepository.findById(appointmentEditDTO.physiotherapistId())
        .orElseThrow(() -> new RuntimeException("Physiotherapist not found"));

    Appointment appointment = appointmentRepository.findByDateTimeAndPhysiotherapist(
        physiotherapist.getId(),
        appointmentEditDTO.dateTime());

    List<AppointmentClient> appointmentClients = appointmentClientRepository.findByAppointment(appointment);

    if (appointmentClients.size() >= 4) {
      throw new RuntimeException("Atendimento não disponível: número máximo de clientes atingido");
    }

    appointmentClient.setAppointment(appointment);
    appointmentClient.setConfirmation(AppointmentConfirmation.CONFIRMED);
    appointmentClient.setAttendance(true);

    return appointmentClient;
  }

  /**
   * Updates the batch of appointments for a specific client.
   * Deletes all existing appointments associated with the client and creates new
   * appointments
   * based on the provided days, times, modality, and other configuration details.
   *
   * @param appointmentEditBatch the {@link AppointmentEditBatchDTO} containing
   *                             the updated configuration
   *                             for the batch of appointments, including days,
   *                             times, modality, and repetition rules.
   * @param clientId             the ID of the client whose appointments are being
   *                             updated.
   * @Transactional Ensures that all database operations in this method are
   * executed atomically.
   */
  @Transactional
  public void updateBatchAppointmentsForClient(AppointmentEditBatchDTO appointmentEditBatch, Long clientId) {
    // Verificar se o cliente existe
    clientRepository.findById(clientId)
        .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));

    // Deletar os atendimentos existentes para o cliente
    appointmentClientRepository.deleteByClientId(clientId);
    appointmentRepository.deleteByClientId(clientId);

    // Iterar sobre os dias e horários fornecidos
    for (Map.Entry<String, List<LocalTime>> entry : appointmentEditBatch.getDaysAndTimes().entrySet()) {
      String dayOfWeek = entry.getKey(); // Exemplo: "terça-feira"
      List<LocalTime> times = entry.getValue(); // Exemplo: [16:00, 18:00]

      for (LocalTime time : times) {
        // Criar o objeto AppointmentCreateDTO
        AppointmentCreateDTO appointmentCreateDTO = new AppointmentCreateDTO();
        appointmentCreateDTO.setData(dayOfWeek); // Dia da semana
        appointmentCreateDTO.setHorario(time.toString()); // Horário no formato HH:mm
        appointmentCreateDTO.setRepetir(appointmentEditBatch.getRepetir()); // Configuração de repetição
        appointmentCreateDTO.setModalidade(appointmentEditBatch.getModality()); // Modalidade
        appointmentCreateDTO.setClientes(List.of(clientId)); // Associar ao cliente atual
        appointmentCreateDTO.setFisioterapeuta(appointmentEditBatch.getPhysiotherapistId()); // Fisioterapeuta
        appointmentCreateDTO.setLocacao(appointmentEditBatch.getLocation()); // Localização

        // Criar o novo AppointmentClient utilizando o método createAppointmentClient
        physiotherapistService.createAppointmentClient(appointmentCreateDTO);
      }
    }
  }

  /**
   * Calculates the number of hours remaining until the specified appointment.
   * Uses the current date and time to compute the difference in hours from the
   * appointment's scheduled date and time.
   *
   * @param appointment the {@link Appointment} object for which the hours are to
   *                    be calculated.
   *                    Must not be null and must contain a valid date and time.
   * @return the number of hours remaining until the appointment. If the
   * appointment's date and time
   * is in the past, the result will be negative.
   */
  public Long calculateHoursUntilAppointments(Appointment appointment) {
    LocalDateTime currentDateTime = LocalDateTime.now();
    return ChronoUnit.HOURS.between(currentDateTime, appointment.getDateTime());
  }

  /**
   * Converts an {@link Appointment} entity to a {@link AppointmentGetDTO},
   * including associated clients
   * and the assigned physiotherapist.
   * Retrieves additional data about clients associated with the appointment from
   * the repository and
   * maps it to a list of {@link AppointmentClientDTO}.
   *
   * @param appointment the {@link Appointment} entity to be converted. Must not
   *                    be null and must
   *                    have valid associated entities (clients and
   *                    physiotherapist).
   * @return an {@link AppointmentGetDTO} containing detailed information about
   * the appointment,
   * including its ID, date/time, location, modality, physiotherapist, and
   * associated clients.
   */
  private AppointmentGetDTO convertToGetDTO(Appointment appointment) {
    List<AppointmentClient> appointmentClients = appointmentClientRepository.findByAppointment(appointment);
    List<AppointmentClientDTO> clients = new ArrayList<>();
    for (AppointmentClient appointmentClient : appointmentClients) {
      AppointmentClientDTO client = new AppointmentClientDTO(
          appointmentClient.getClient().getId(),
          appointmentClient.getClient().getName(),
          appointmentClient.getConfirmation(),
          appointmentClient.getAttendance());

      clients.add(client);
    }

    AppointmentPhysiotherapistDTO physiotherapist = new AppointmentPhysiotherapistDTO(
        appointment.getPhysiotherapist().getId(),
        appointment.getPhysiotherapist().getName());

    return new AppointmentGetDTO(
        appointment.getId(),
        appointment.getDateTime(),
        appointment.getLocation(),
        appointment.getModality(),
        physiotherapist,
        clients);
  }

  /**
   * Converts an {@link Appointment} entity to a {@link AppointmentGetDTO},
   * including associated filtered clients
   * and the assigned physiotherapist.
   * Allows a pre-filtered list of {@link AppointmentClient} to be provided,
   * optimizing the conversion process
   * by avoiding additional repository queries.
   *
   * @param appointment     the {@link Appointment} entity to be converted. Must
   *                        not be null and must
   *                        have valid associated entities (e.g.,
   *                        physiotherapist).
   * @param filteredClients a pre-filtered list of {@link AppointmentClient}
   *                        entities associated with the
   *                        appointment. Each client in the list will be included
   *                        in the resulting DTO.
   * @return an {@link AppointmentGetDTO} containing detailed information about
   * the appointment,
   * including its ID, date/time, location, modality, physiotherapist, and
   * the filtered list of clients.
   */
  private AppointmentGetDTO convertToGetDTO(Appointment appointment, List<AppointmentClient> filteredClients) {
    List<AppointmentClientDTO> clients = filteredClients.stream()
        .map(appointmentClient -> new AppointmentClientDTO(
            appointmentClient.getClient().getId(),
            appointmentClient.getClient().getName(),
            appointmentClient.getConfirmation(),
            appointmentClient.getAttendance()))
        .collect(Collectors.toList());

    AppointmentPhysiotherapistDTO physiotherapist = new AppointmentPhysiotherapistDTO(
        appointment.getPhysiotherapist().getId(),
        appointment.getPhysiotherapist().getName());

    return new AppointmentGetDTO(
        appointment.getId(),
        appointment.getDateTime(),
        appointment.getLocation(),
        appointment.getModality(),
        physiotherapist,
        clients);
  }
}
