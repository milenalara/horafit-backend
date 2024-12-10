package com.horafit.backend.service;

import com.horafit.backend.util.exception.appointment.AppointmentsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.horafit.backend.entity.AppointmentClient;
import com.horafit.backend.repository.AppointmentClientRepository;

@Service
public class AppointmentClientService {
  @Autowired
  AppointmentClientRepository appointmentClientRepository;

  @Transactional
  public AppointmentClient absentClient(Long clientId, Long appointmentId) {
    AppointmentClient appointmentClient = appointmentClientRepository
        .findAppointmentClientByClient_IdAndAppointment_Id(clientId, appointmentId)
        .orElseThrow(() -> new AppointmentsException.AppointmentNotFoundException(
            "Appointment not found for client with id = " + clientId));

    if (appointmentClient == null) {
      throw new RuntimeException("AppointmentClient not found");
    }
    appointmentClient.setAttendance(false);

    return appointmentClientRepository.save(appointmentClient);
  }
}
