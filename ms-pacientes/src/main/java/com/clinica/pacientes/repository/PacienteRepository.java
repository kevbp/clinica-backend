package com.clinica.pacientes.repository;

import com.clinica.pacientes.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
}
