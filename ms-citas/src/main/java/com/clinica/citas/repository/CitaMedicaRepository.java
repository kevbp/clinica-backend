package com.clinica.citas.repository;

import com.clinica.citas.model.CitaMedica;
import com.clinica.citas.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaMedicaRepository extends JpaRepository<CitaMedica, Long> {

    // Para Lazy Evaluation: citas ocupadas de un médico en un día dado
    List<CitaMedica> findByIdPersonalAndFechaHoraBetweenAndEstadoNot(
            Long idPersonal, LocalDateTime inicio, LocalDateTime fin, EstadoCita estadoExcluido);
}
