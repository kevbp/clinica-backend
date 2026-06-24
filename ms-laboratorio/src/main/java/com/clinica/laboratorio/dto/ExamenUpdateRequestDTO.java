package com.clinica.laboratorio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Campos actualizables de un examen. Solo se modifican los campos no nulos.")
public class ExamenUpdateRequestDTO {

    @Schema(description = "Nombre del examen", example = "Hemograma Completo")
    private String nombre;

    @Schema(description = "Categoría del examen", example = "Hematología")
    private String categoria;

    @Schema(description = "Descripción", example = "Análisis de componentes sanguíneos")
    private String descripcion;

    @Schema(description = "Precio de referencia", example = "45.00")
    private BigDecimal precio;
}
