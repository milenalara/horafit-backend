package com.horafit.backend.dto.businessRules;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.horafit.backend.entity.Rule;

@Getter
@Setter
public class BusinessRulesRegisterDTO {

    private String title;
    private List<Rule> rules;

    public BusinessRulesRegisterDTO( String title, List<Rule> rules){
        this.title = title;
        this.rules = rules;
    }
}
