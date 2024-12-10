package com.horafit.backend.dto.payment;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UpdatePaymentDTO {
    private Long clientId;
    private Date date;
}
