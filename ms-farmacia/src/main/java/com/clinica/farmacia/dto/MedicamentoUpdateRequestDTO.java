package com.clinica.farmacia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Campos actualizables de un medicamento. Solo se modifican los campos no nulos.")
public class MedicamentoUpdateRequestDTO {

    @Schema(description = "Nombre comercial", example = "Amoxicilina 500mg")
    private String nombre;

    @Schema(description = "Principio activo", example = "Amoxicilina")
    private String principioActivo;

    @Schema(description = "Presentación", example = "Cápsulas")
    private String presentacion;

    @Schema(description = "Precio de venta", example = "12.50")
    private BigDecimal precio;
}
