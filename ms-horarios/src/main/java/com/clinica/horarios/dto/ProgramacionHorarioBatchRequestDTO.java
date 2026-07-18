package com.clinica.horarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Schema(description = "Datos para crear turnos en múltiples fechas en una sola operación transaccional")
public class ProgramacionHorarioBatchRequestDTO {

    @NotNull
    @Schema(description = "ID del personal médico", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idPersonal;

    @NotNull
    @Schema(description = "ID del consultorio físico", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idConsultorio;

    @NotNull
    @NotEmpty
    @Schema(description = "Lista de fechas para las que se creará el turno. Si alguna falla, ninguna se crea.",
            example = "[\"2026-07-14\", \"2026-07-15\", \"2026-07-16\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<LocalDate> fechas;

    @NotNull
    @Schema(description = "Hora de inicio del turno (aplica igual para todas las fechas)", example = "08:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime horaInicio;

    @NotNull
    @Schema(description = "Hora de fin del turno (aplica igual para todas las fechas)", example = "13:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime horaFin;
}
