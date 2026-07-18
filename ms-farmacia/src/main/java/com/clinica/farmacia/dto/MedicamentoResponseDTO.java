package com.clinica.farmacia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Datos de un medicamento del catálogo")
public class MedicamentoResponseDTO {

    @Schema(description = "ID interno del medicamento", example = "104")
    private Long id;

    @Schema(description = "Nombre comercial", example = "Amoxicilina 500mg")
    private String nombre;

    @Schema(description = "Principio activo", example = "Amoxicilina")
    private String principioActivo;

    @Schema(description = "Presentación", example = "Caja x 20 tabletas")
    private String presentacion;

    @Schema(description = "Precio unitario en soles", example = "15.50")
    private java.math.BigDecimal precio;
}
