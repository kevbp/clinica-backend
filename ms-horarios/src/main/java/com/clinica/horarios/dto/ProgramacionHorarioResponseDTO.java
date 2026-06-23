package com.clinica.horarios.dto;

import com.clinica.horarios.model.DiaSemana;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;

@Data
@Schema(description = "Turno maestro de programación horaria")
public class ProgramacionHorarioResponseDTO {

    @Schema(description = "ID interno del turno", example = "10")
    private Long id;

    @Schema(description = "ID del personal médico (referencia débil a ms-personal)", example = "5")
    private Long idPersonal;

    @Schema(description = "Consultorio físico asignado")
    private ConsultorioResponseDTO consultorio;

    @Schema(description = "Día de la semana", example = "LUNES")
    private DiaSemana diaSemana;

    @Schema(description = "Hora de inicio del turno", example = "08:00")
    private LocalTime horaInicio;

    @Schema(description = "Hora de fin del turno", example = "13:00")
    private LocalTime horaFin;
}
