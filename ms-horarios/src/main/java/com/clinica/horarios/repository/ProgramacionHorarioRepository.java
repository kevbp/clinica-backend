package com.clinica.horarios.repository;

import com.clinica.horarios.model.DiaSemana;
import com.clinica.horarios.model.ProgramacionHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface ProgramacionHorarioRepository extends JpaRepository<ProgramacionHorario, Long> {

    List<ProgramacionHorario> findByIdPersonal(Long idPersonal);

    // Detecta si otro idPersonal ya ocupa ese consultorio en la misma franja.
    // Dos rangos [a,b) y [c,d) solapan cuando a < d AND c < b.
    @Query("""
            SELECT ph FROM ProgramacionHorario ph
            WHERE ph.consultorio.id = :idConsultorio
              AND ph.diaSemana     = :diaSemana
              AND ph.idPersonal   <> :idPersonal
              AND ph.horaInicio    < :horaFin
              AND ph.horaFin       > :horaInicio
            """)
    List<ProgramacionHorario> findConflictos(
            @Param("idConsultorio") Long idConsultorio,
            @Param("diaSemana")    DiaSemana diaSemana,
            @Param("idPersonal")   Long idPersonal,
            @Param("horaInicio")   LocalTime horaInicio,
            @Param("horaFin")      LocalTime horaFin);
}
