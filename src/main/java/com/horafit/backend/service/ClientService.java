package com.horafit.backend.service;

import com.horafit.backend.dto.client.ClientRegisterDTO;
import com.horafit.backend.dto.client.ResetPasswordDTO;
import com.horafit.backend.entity.AppointmentRules;
import com.horafit.backend.entity.BusinessRules;
import com.horafit.backend.entity.Client;
import com.horafit.backend.repository.AppointmentRulesRepository;
import com.horafit.backend.repository.BusinessRulesRepository;
import com.horafit.backend.repository.ClientRepository;
import com.horafit.backend.util.PasswordUtil;
import com.horafit.backend.util.exception.client.ClientException.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AppointmentRulesService appointmentRulesService;

    @Autowired
    private BusinessRulesRepository businessRulesRepository;
  @Autowired
  private AppointmentRulesRepository appointmentRulesRepository;

    public Client register(ClientRegisterDTO dto) {
        if (clientRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new EmailNotFoundException("Email já cadastrado");
        }

        PasswordUtil encrypted = new PasswordUtil();
        String encryptedPassword = encrypted.encoder(dto.getPassword());

        Client client = new Client();
        client.setEmail(dto.getEmail());
        client.setName(dto.getName());
        client.setPassword(encryptedPassword);

        // O cliente vai ser criado com a regra de pilates em grupo 2x na semana por padrão
        // provisório
        AppointmentRules appointmentRules = appointmentRulesRepository.findById(2L)
            .orElseThrow(()-> new RuntimeException("Appointment Rule not found"));

        client.setAppointmentRules(appointmentRules);
        return clientRepository.save(client);
    }

    public void resetPassword(ResetPasswordDTO dto) {
        Client client = clientRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new EmailNotFoundException("Email não encontrado"));

        client.setPassword(dto.getNewPassword());
        clientRepository.save(client);
    }

    public void acceptContract(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Cliente não encontrado com ID: " + id));

        if (client.getSignedContract() != null) {
            throw new ContractAlreadySigned("Contrato já aceito anteriormente para o cliente com ID: " + id);
        }

        client.setSignedContract(new Date());
        clientRepository.save(client);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client getById(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void updateClientAppointmentRules(String emailClient, Long idAppointmentRules) {
        Client client = clientRepository.findByEmail(emailClient)
                .orElseThrow(() -> new EmailNotFoundException("Email não encontrado"));

        AppointmentRules appointmentRules = appointmentRulesService.findById(idAppointmentRules);

        client.setAppointmentRules(appointmentRules);
        clientRepository.save(client);
    }

    public List<BusinessRules> showBusinessRules() {
        return businessRulesRepository.findAll();
    }

    public Boolean contractStatus(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EmailNotFoundException("Id não encontrado"));

        if (client.getSignedContract() == null) {
            return false;
        } else {
            return true;
        }
    }

}
