package com.clinica.farmacia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Datos para registrar un medicamento en el catálogo")
public class MedicamentoRequestDTO {

    @NotBlank
    @Schema(description = "Nombre comercial del medicamento", example = "Amoxicilina 500mg",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @NotBlank
    @Schema(description = "Principio activo", example = "Amoxicilina",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String principioActivo;

    @NotBlank
    @Schema(description = "Presentación", example = "Caja x 20 tabletas",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String presentacion;

    @NotNull
    @DecimalMin(value = "0.01")
    @Schema(description = "Precio de venta (solo visible vía /precio, exclusivo para ms-caja)",
            example = "25.50", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal precio;
}
