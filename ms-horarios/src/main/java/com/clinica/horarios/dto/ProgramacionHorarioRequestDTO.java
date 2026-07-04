package com.clinica.horarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "Datos para crear un turno de programación horaria en una fecha concreta")
public class ProgramacionHorarioRequestDTO {

    @NotNull
    @Schema(description = "ID del personal médico en ms-personal (referencia débil)",
            example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idPersonal;

    @NotNull
    @Schema(description = "ID del consultorio físico",
            example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idConsultorio;

    @NotNull
    @Schema(description = "Fecha concreta del turno. No puede ser anterior a hoy.",
            example = "2026-07-06", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate fecha;

    @NotNull
    @Schema(description = "Hora de inicio del turno", example = "08:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime horaInicio;

    @NotNull
    @Schema(description = "Hora de fin del turno", example = "13:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime horaFin;
}
