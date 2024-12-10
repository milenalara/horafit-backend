package com.horafit.backend.dto.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientSimpleDTO {
    private Long id;
    private String name;

    public ClientSimpleDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
