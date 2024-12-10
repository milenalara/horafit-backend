package com.horafit.backend.controller;

import com.horafit.backend.dto.user.UserLoginDTO;
import com.horafit.backend.dto.user.UserLoginResponseDTO;
import com.horafit.backend.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
@Tag(name = "User API", description = "API para operações relacionadas a login e autenticação")
public class UserController {
  @Autowired
  UserService userService;
  @PostMapping("/login")
  public ResponseEntity<UserLoginResponseDTO> login(@RequestBody UserLoginDTO requestDTO) {
    UserLoginResponseDTO responseDTO = userService.login(requestDTO);
    return ResponseEntity.ok(responseDTO);
  }
}
