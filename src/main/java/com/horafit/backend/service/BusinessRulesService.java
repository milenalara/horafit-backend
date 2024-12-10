package com.horafit.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.horafit.backend.dto.businessRules.BusinessRulesRegisterDTO;
import com.horafit.backend.entity.BusinessRules;
import com.horafit.backend.entity.Rule;
import com.horafit.backend.repository.BusinessRulesRepository;

@Service
public class BusinessRulesService {
    @Autowired
    BusinessRulesRepository businessRulesRepository;


public BusinessRules register(BusinessRulesRegisterDTO dto) {
    BusinessRules businessRules = new BusinessRules();
    businessRules.setTitle(dto.getTitle());
    
    List<Rule> rules = new ArrayList<>();
    for (Rule dtoRule : dto.getRules()) {
        Rule rule = new Rule();
        rule.setRuleText(dtoRule.getRuleText()); 
        rule.setBusinessRules(businessRules); 
        rules.add(rule);
    }
    
    businessRules.setRules(rules);
    
    return businessRulesRepository.save(businessRules);
}
}
