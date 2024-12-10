package com.horafit.backend.repository;

import com.horafit.backend.entity.Physiotherapist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PhysiotherapistRepository extends JpaRepository<Physiotherapist, Long> {
  Optional<Physiotherapist> findByEmail(String email);
  boolean existsByEmail(String email);
}
