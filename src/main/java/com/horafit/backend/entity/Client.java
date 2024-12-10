package com.horafit.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "client")
@Getter
@Setter
public class Client extends User{
    @Column(name = "signed_contract")
    private java.util.Date signedContract;

    @ManyToOne
    @JoinColumn(name = "appointment_rules", referencedColumnName = "id")
    private AppointmentRules appointmentRules;
}
