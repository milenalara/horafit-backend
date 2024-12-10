package com.horafit.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.horafit.backend.entity.BusinessRules;

@Repository
public interface BusinessRulesRepository extends JpaRepository<BusinessRules, Long> {
    
}
