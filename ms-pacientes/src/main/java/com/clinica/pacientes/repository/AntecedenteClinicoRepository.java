package com.clinica.pacientes.repository;

import com.clinica.pacientes.model.AntecedenteClinico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AntecedenteClinicoRepository extends JpaRepository<AntecedenteClinico, Long> {

    List<AntecedenteClinico> findByPacienteId(Long idPaciente);
}
