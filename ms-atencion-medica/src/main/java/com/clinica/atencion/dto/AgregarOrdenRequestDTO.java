package com.clinica.atencion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Línea de examen a agregar a la orden de laboratorio del borrador")
public class AgregarOrdenRequestDTO {

    @NotNull
    @Schema(description = "ID del examen en ms-laboratorio", example = "12",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idExamen;

    @Schema(description = "Indicaciones de preparación para el paciente",
            example = "Ayuno de 8 horas")
    private String indicacionesPreparacion;
}
