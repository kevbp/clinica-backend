package com.clinica.horarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "Campos actualizables de un turno. Solo se modifican los campos enviados (no nulos). " +
        "No se puede modificar un turno cuya fecha (actual o nueva) sea anterior a hoy.")
public class ProgramacionHorarioUpdateRequestDTO {

    @Schema(description = "ID del consultorio físico", example = "1")
    private Long idConsultorio;

    @Schema(description = "Nueva fecha concreta del turno", example = "2026-07-07")
    private LocalDate fecha;

    @Schema(description = "Hora de inicio del turno", example = "08:00")
    private LocalTime horaInicio;

    @Schema(description = "Hora de fin del turno", example = "13:00")
    private LocalTime horaFin;
}
