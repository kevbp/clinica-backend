package com.clinica.citas.repository;

import com.clinica.citas.model.CitaMedica;
import com.clinica.citas.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaMedicaRepository extends JpaRepository<CitaMedica, Long> {

    // Para Lazy Evaluation: citas ocupadas de un médico en un día dado
    List<CitaMedica> findByIdPersonalAndFechaHoraBetweenAndEstadoNot(
            Long idPersonal, LocalDateTime inicio, LocalDateTime fin, EstadoCita estadoExcluido);

    // Listado con filtros opcionales para el frontend
    @Query("""
            SELECT c FROM CitaMedica c
            WHERE (:idPaciente IS NULL OR c.idPaciente = :idPaciente)
              AND (:idPersonal IS NULL OR c.idPersonal = :idPersonal)
              AND (:estado IS NULL OR c.estado = :estado)
              AND (:inicio IS NULL OR c.fechaHora >= :inicio)
              AND (:fin IS NULL OR c.fechaHora < :fin)
            ORDER BY c.fechaHora ASC
            """)
    List<CitaMedica> buscarConFiltros(
            @Param("idPaciente") Long idPaciente,
            @Param("idPersonal") Long idPersonal,
            @Param("estado") EstadoCita estado,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}
