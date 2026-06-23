package com.clinica.laboratorio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Examen autorizado tras la confirmación de pago en ms-caja")
public class ExamenAutorizadoResponseDTO {

    @Schema(description = "ID interno de la autorización", example = "55")
    private Long id;

    @Schema(description = "ID del paciente", example = "42")
    private Long idPaciente;

    @Schema(description = "ID del episodio clínico (ObjectId MongoDB)",
            example = "64a1f3b2e4b0c72a9d8e1f0a")
    private String idEpisodioClinico;

    @Schema(description = "ID del examen autorizado", example = "12")
    private Long idExamen;

    @Schema(description = "Nombre del examen autorizado", example = "Hemograma completo")
    private String nombreExamen;

    @Schema(description = "Fecha y hora de la autorización", example = "2024-06-15T10:30:00")
    private LocalDateTime fechaAutorizacion;
}
