package com.clinica.horarios.repository;

import com.clinica.horarios.model.Consultorio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultorioRepository extends JpaRepository<Consultorio, Long> {
}
