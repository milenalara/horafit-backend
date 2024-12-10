package com.horafit.backend.service;

import com.fasterxml.jackson.dataformat.yaml.UTF8Reader;
import com.horafit.backend.dto.user.UserLoginDTO;
import com.horafit.backend.dto.user.UserLoginResponseDTO;
import com.horafit.backend.entity.Client;
import com.horafit.backend.entity.Physiotherapist;
import com.horafit.backend.repository.ClientRepository;
import com.horafit.backend.repository.PhysiotherapistRepository;

import com.horafit.backend.repository.PhysiotherapistRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  @Autowired
  private ClientRepository clientRepository;

  @Autowired
  private PhysiotherapistRepository physiotherapistRepository;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  public UserLoginResponseDTO login (UserLoginDTO requestDTO) {
    if (physiotherapistRepository.existsByEmail(requestDTO.email())) {
      Physiotherapist physiotherapist = physiotherapistRepository.findByEmail(requestDTO.email())
          .orElseThrow(() -> new RuntimeException("Physitherapist not found"));

        if (!physiotherapist.getPassword().equals(requestDTO.password())) {
          throw new RuntimeException("Incorrect password");
        }

      return new UserLoginResponseDTO(
          physiotherapist.getId(),
          physiotherapist.getClass().getSimpleName()
      );
    }

    if(clientRepository.existsByEmail(requestDTO.email())) {
      Client client = clientRepository.findByEmail(requestDTO.email())
          .orElseThrow(() -> new RuntimeException("Client not found"));

      //Comparando a senha com criptografia
       if (!passwordEncoder.matches(requestDTO.password(), client.getPassword())) {
                throw new RuntimeException("Incorrect password");
            }

      return new UserLoginResponseDTO(
          client.getId(),
          client.getClass().getSimpleName()
      );

    }

    throw new RuntimeException("User not found");
  }
}
