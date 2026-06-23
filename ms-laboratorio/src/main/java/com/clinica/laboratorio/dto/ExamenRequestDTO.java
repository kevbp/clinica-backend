package com.clinica.laboratorio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Datos para registrar un examen en el catálogo")
public class ExamenRequestDTO {

    @NotBlank
    @Schema(description = "Nombre del examen clínico", example = "Hemograma completo",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @NotBlank
    @Schema(description = "Categoría del examen", example = "Hematología",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String categoria;

    @Schema(description = "Descripción del examen",
            example = "Análisis de los componentes de la sangre")
    private String descripcion;

    @NotNull
    @DecimalMin(value = "0.01")
    @Schema(description = "Precio del examen (solo visible vía /precio, exclusivo para ms-caja)",
            example = "45.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal precio;
}
