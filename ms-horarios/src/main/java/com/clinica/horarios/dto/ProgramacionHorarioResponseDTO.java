package com.clinica.horarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "Turno de programación horaria en una fecha concreta")
public class ProgramacionHorarioResponseDTO {

    @Schema(description = "ID interno del turno", example = "10")
    private Long id;

    @Schema(description = "ID del personal médico (referencia débil a ms-personal)", example = "5")
    private Long idPersonal;

    @Schema(description = "Consultorio físico asignado")
    private ConsultorioResponseDTO consultorio;

    @Schema(description = "Fecha concreta del turno", example = "2026-07-06")
    private LocalDate fecha;

    @Schema(description = "Hora de inicio del turno", example = "08:00")
    private LocalTime horaInicio;

    @Schema(description = "Hora de fin del turno", example = "13:00")
    private LocalTime horaFin;

    @Schema(description = "Indica si la fecha del turno ya pasó (no editable/eliminable)", example = "false")
    private Boolean esPasado;
}
