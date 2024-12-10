package com.horafit.backend.service;

import com.horafit.backend.dto.appointment.AddClientToAppointmentDTO;
import com.horafit.backend.dto.appointment.AppointmentCreateDTO;
import com.horafit.backend.dto.appointment.AppointmentUpdateDTO;
import com.horafit.backend.entity.Appointment;
import com.horafit.backend.entity.AppointmentClient;
import com.horafit.backend.entity.Client;
import com.horafit.backend.entity.Physiotherapist;
import com.horafit.backend.entity.enums.AppointmentConfirmation;
import com.horafit.backend.entity.enums.AppointmentModality;
import com.horafit.backend.entity.enums.AppointmentLocation;
import com.horafit.backend.repository.AppointmentClientRepository;
import com.horafit.backend.repository.AppointmentRepository;
import com.horafit.backend.repository.ClientRepository;
import com.horafit.backend.repository.PhysiotherapistRepository;
import com.horafit.backend.util.exception.appointment.AppointmentsException;
import com.horafit.backend.util.exception.appointment.AppointmentsException.*;
import com.horafit.backend.util.exception.client.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PhysiotherapistService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentClientRepository appointmentClientRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PhysiotherapistRepository physiotherapistRepository;

    public void createAppointmentClient(AppointmentCreateDTO appointmentDTO) {
        LocalDate date = LocalDate.parse(appointmentDTO.getData(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        LocalTime time = LocalTime.parse(appointmentDTO.getHorario(), DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        List<Client> clients = new ArrayList<>();
        for (Long clientId : appointmentDTO.getClientes()) {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new IllegalArgumentException("Client not found: " + clientId));
            clients.add(client);
        }

        Physiotherapist physiotherapist = physiotherapistRepository.findById(appointmentDTO.getFisioterapeuta())
                .orElseThrow(() -> new IllegalArgumentException("Fisioterapeuta não encontrado"));

        Long hasPhysioAppointment = appointmentClientRepository.existsByPhysiotherapistIdAndDateTime(physiotherapist.getId(), dateTime);
        if (hasPhysioAppointment > 0) {
            throw new AppointmentAlreadyExistsException("Já existe um atendimento agendado para o fisioterapeuta " + physiotherapist.getName() + " neste dia e horário.");
        }

        if (clients.isEmpty()) {
            Appointment appointment = createEmptyAppointment(dateTime, physiotherapist, appointmentDTO);
            appointmentRepository.save(appointment);
        } else {
            for (Client client : clients) {
                Long hasExistingAppointment = appointmentClientRepository.existsByClientIdAndDateTime(client.getId(), dateTime);
                if (hasExistingAppointment > 0) {
                    throw new AppointmentAlreadyExistsException("Já existe um atendimento agendado para o cliente " + client.getName() + " neste dia e horário.");
                }
            }

            List<Appointment> appointments = new ArrayList<>();
            if (appointmentDTO.getRepetir().isAtivo()) {
                String opcao = appointmentDTO.getRepetir().getOpcao();
                if ("Sempre".equalsIgnoreCase(opcao)) {
                    createWeeklyAppointmentsForSixMonths(dateTime, clients, physiotherapist, appointmentDTO, appointments);
                } else if ("X vezes".equalsIgnoreCase(opcao)) {
                    createAppointmentsByQuantity(dateTime, clients, physiotherapist, appointmentDTO, appointments);
                }
            } else {
                appointments.add(createAppointment(dateTime, clients, physiotherapist, appointmentDTO));
            }
            appointmentRepository.saveAll(appointments);
        }
    }

    private void createWeeklyAppointmentsForSixMonths(LocalDateTime dateTime, List<Client> clients, Physiotherapist physio, AppointmentCreateDTO dto, List<Appointment> appointments) {
        for (int i = 0; i < 26; i++) { // 6 meses -> 26 semanas
            Appointment appointment = createAppointment(dateTime.plusWeeks(i), clients, physio, dto);
            appointments.add(appointment);
        }
    }

    private void createAppointmentsByQuantity(LocalDateTime dateTime, List<Client> clients, Physiotherapist physio, AppointmentCreateDTO dto, List<Appointment> appointments) {
        for (int i = 0; i < dto.getRepetir().getQuantidade(); i++) {
            Appointment appointment = createAppointment(dateTime.plusWeeks(i), clients, physio, dto);
            appointments.add(appointment);
        }
    }

    private Appointment createAppointment(LocalDateTime dateTime, List<Client> clients, Physiotherapist physio, AppointmentCreateDTO dto) {
        Appointment appointment = new Appointment();
        appointment.setDateTime(dateTime);
        appointment.setPhysiotherapist(physio);
        appointment.setModality(AppointmentModality.valueOf(dto.getModalidade().toUpperCase()));
        appointment.setLocation(AppointmentLocation.valueOf(dto.getLocacao().toUpperCase()));

        appointment = appointmentRepository.save(appointment);

        for (Client client : clients) {
            AppointmentClient appointmentClient = new AppointmentClient();
            appointmentClient.setAppointment(appointment);
            appointmentClient.setClient(client);
            appointmentClient.setConfirmation(AppointmentConfirmation.CONFIRMED);
            appointmentClient.setAttendance(true);

            appointmentClientRepository.save(appointmentClient);
        }

        return appointment;
    }

    private Appointment createEmptyAppointment(LocalDateTime dateTime, Physiotherapist physio, AppointmentCreateDTO dto) {
            Appointment appointment = new Appointment();
            appointment.setDateTime(dateTime);
            appointment.setModality(AppointmentModality.valueOf(dto.getModalidade().toUpperCase()));
            appointment.setLocation(AppointmentLocation.valueOf(dto.getLocacao().toUpperCase()));
            appointment.setPhysiotherapist(physio);

            return appointmentRepository.save(appointment);
    }

    public void updateAppointment(AppointmentUpdateDTO appointmentUpdateDTO) {
        Appointment appointment = appointmentRepository.findById(appointmentUpdateDTO.getIdAtendimento())
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado: " + appointmentUpdateDTO.getIdAtendimento()));

        Long clientCount = appointmentClientRepository.countByAppointmentId(appointment.getId());

        LocalDate date = LocalDate.parse(appointmentUpdateDTO.getData(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        LocalTime time = LocalTime.parse(appointmentUpdateDTO.getHorario(), DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime newDateTime = LocalDateTime.of(date, time);

        Long hasPhysioAppointment = appointmentClientRepository.existsByPhysiotherapistIdAndDateTime(appointment.getPhysiotherapist().getId(), newDateTime);
        if (hasPhysioAppointment > 0) {
            throw new AppointmentAlreadyExistsException("Já existe um atendimento agendado para este fisioterapeuta neste dia e horário.");
        }

        Long hasExistingAppointment = appointmentClientRepository.existsByClientIdAndDateTime(appointmentUpdateDTO.getIdCliente(), newDateTime);
        if (hasExistingAppointment > 0) {
            throw new AppointmentAlreadyExistsException("Já existe um atendimento agendado para o cliente neste dia e horário.");
        }

        if (clientCount > 1) {
            AppointmentClient clientToRemove = appointmentClientRepository.findByAppointmentIdAndClientId(
                            appointment.getId(), appointmentUpdateDTO.getIdCliente())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado no atendimento especificado"));

            appointmentClientRepository.delete(clientToRemove);

            Appointment newAppointment = new Appointment();
            newAppointment.setDateTime(newDateTime);
            newAppointment.setPhysiotherapist(appointment.getPhysiotherapist());
            newAppointment.setModality(AppointmentModality.valueOf(appointmentUpdateDTO.getModalidade()));
            newAppointment.setLocation(appointment.getLocation());
            appointmentRepository.save(newAppointment);

            AppointmentClient newAppointmentClient = new AppointmentClient();
            newAppointmentClient.setAppointment(newAppointment);
            newAppointmentClient.setClient(clientToRemove.getClient());
            newAppointmentClient.setConfirmation(AppointmentConfirmation.CONFIRMED);
            newAppointmentClient.setAttendance(true);
            appointmentClientRepository.save(newAppointmentClient);

        } else {
            appointment.setModality(AppointmentModality.valueOf(appointmentUpdateDTO.getModalidade()));
            appointment.setDateTime(newDateTime);
            appointmentRepository.save(appointment);
        }
    }

    public void addClientsToAppointment(Long appointmentId, List<Long> clientIds) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentsException.AppointmentNotFoundException("Atendimento não encontrado"));

        for (Long clientId : clientIds) {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new ClientException.ClientNotFoundException("Cliente com ID " + clientId + " não encontrado"));

            boolean alreadyExists = appointmentClientRepository.existsByAppointmentIdAndClientId(appointment.getId(), client.getId());
            if (alreadyExists) {
                throw new ClientException.ClientAlreadyExistsException("Cliente com ID " + clientId + " já está associado a este atendimento");
            }

            AppointmentClient appointmentClient = new AppointmentClient();
            appointmentClient.setAppointment(appointment);
            appointmentClient.setClient(client);
            appointmentClient.setConfirmation(AppointmentConfirmation.CONFIRMED);
            appointmentClient.setAttendance(true);

            appointmentClientRepository.save(appointmentClient);
        }
    }

    public void deleteClientFromAppointment(Long clientId, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentsException.AppointmentDeleteException("Atendimento não encontrado com ID: " + appointmentId));

        clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado - ID: " + clientId));

        AppointmentClient appointmentClient = appointmentClientRepository
            .findAppointmentClientByClient_IdAndAppointment_Id(clientId, appointmentId)
            .orElseThrow(() -> new AppointmentsException.AppointmentNotFoundException(
                "Appointment not found for client with id = " + clientId));


        if (appointmentClient == null) {
            throw new RuntimeException("Não há horário agendado para o cliente informado neste atendimento");
        }

        long clientCount = appointmentClientRepository.countByAppointmentId(appointmentId);

        if (clientCount > 1) {
            appointmentClientRepository.delete(appointmentClient);
        } else {
            appointmentClientRepository.delete(appointmentClient);
            appointmentRepository.delete(appointment);
        }
    }

    public List<Client> findClientsByName(String name) {
        return clientRepository.findClientsWithAppointmentsByName(name);
    }
}
