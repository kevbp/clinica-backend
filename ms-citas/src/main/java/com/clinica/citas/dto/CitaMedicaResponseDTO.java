package com.clinica.citas.dto;

import com.clinica.citas.model.EstadoCita;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Datos de una cita médica")
public class CitaMedicaResponseDTO {

    @Schema(description = "ID interno de la cita", example = "100")
    private Long id;

    @Schema(description = "ID del paciente", example = "42")
    private Long idPaciente;

    @Schema(description = "ID del médico", example = "5")
    private Long idPersonal;

    @Schema(description = "ID del consultorio", example = "1")
    private Long idConsultorio;

    @Schema(description = "Fecha y hora de la cita", example = "2024-07-10T09:00:00")
    private LocalDateTime fechaHora;

    @Schema(description = "Estado actual de la cita", example = "PENDIENTE_PAGO")
    private EstadoCita estado;
}
