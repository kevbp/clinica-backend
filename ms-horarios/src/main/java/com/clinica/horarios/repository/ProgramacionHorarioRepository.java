package com.clinica.horarios.repository;

import com.clinica.horarios.model.ProgramacionHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ProgramacionHorarioRepository extends JpaRepository<ProgramacionHorario, Long> {

    List<ProgramacionHorario> findByIdPersonal(Long idPersonal);

    List<ProgramacionHorario> findByIdPersonalAndFechaBetween(Long idPersonal, LocalDate desde, LocalDate hasta);

    List<ProgramacionHorario> findByConsultorioId(Long idConsultorio);

    boolean existsByConsultorioId(Long idConsultorio);

    // Detecta si otro idPersonal ya ocupa ese consultorio en la misma fecha y franja.
    // Dos rangos [a,b) y [c,d) solapan cuando a < d AND c < b.
    // :idExcluir permite excluir el propio turno al validar una edición (puede ser null).
    @Query("""
            SELECT ph FROM ProgramacionHorario ph
            WHERE ph.consultorio.id = :idConsultorio
              AND ph.fecha          = :fecha
              AND ph.idPersonal    <> :idPersonal
              AND ph.horaInicio     < :horaFin
              AND ph.horaFin        > :horaInicio
              AND (:idExcluir IS NULL OR ph.id <> :idExcluir)
            """)
    List<ProgramacionHorario> findConflictos(
            @Param("idConsultorio") Long idConsultorio,
            @Param("fecha")         LocalDate fecha,
            @Param("idPersonal")    Long idPersonal,
            @Param("horaInicio")    LocalTime horaInicio,
            @Param("horaFin")       LocalTime horaFin,
            @Param("idExcluir")     Long idExcluir);
}
