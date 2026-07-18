package com.clinica.laboratorio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Datos de un examen del catálogo")
public class ExamenResponseDTO {

    @Schema(description = "ID interno del examen", example = "12")
    private Long id;

    @Schema(description = "Nombre del examen clínico", example = "Hemograma completo")
    private String nombre;

    @Schema(description = "Categoría del examen", example = "Hematología")
    private String categoria;

    @Schema(description = "Descripción del examen",
            example = "Análisis de los componentes de la sangre")
    private String descripcion;

    @Schema(description = "Precio del examen en soles", example = "45.00")
    private java.math.BigDecimal precio;
}
