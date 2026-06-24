package com.clinica.laboratorio.repository;

import com.clinica.laboratorio.model.ExamenAutorizado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamenAutorizadoRepository extends JpaRepository<ExamenAutorizado, Long> {

    List<ExamenAutorizado> findByIdPaciente(Long idPaciente);
}
