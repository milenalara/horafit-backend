package com.horafit.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "physiotherapist")
@Getter
@Setter
public class Physiotherapist extends User {

}
