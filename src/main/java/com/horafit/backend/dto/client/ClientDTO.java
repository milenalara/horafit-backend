package com.horafit.backend.dto.client;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ClientDTO {
    private Long id;
    private String name;
    private String email;
    private Date signedContract;

    public ClientDTO(Long id, String name, String email, Date signedContract){
        this.id = id;
        this.name = name;
        this.email = email;
        this.signedContract = signedContract;
    }
}
