package com.clinica.historias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Línea de orden de laboratorio")
public class LineaOrdenResponseDTO {

    @Schema(description = "ID del examen en ms-laboratorio", example = "12")
    private Long idExamen;

    @Schema(description = "Nombre del examen", example = "Hemograma completo")
    private String nombreExamen;

    @Schema(description = "Categoría del examen", example = "Hematología")
    private String categoria;

    @Schema(description = "Indicaciones de preparación", example = "Ayuno de 8 horas")
    private String indicacionesPreparacion;
}
