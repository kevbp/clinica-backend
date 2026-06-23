package com.clinica.farmacia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Schema(description = "Precio vigente de un medicamento. Endpoint exclusivo para ms-caja.")
public class PrecioResponseDTO {

    @Schema(description = "ID del medicamento", example = "104")
    private Long idMedicamento;

    @Schema(description = "Precio de venta", example = "25.50")
    private BigDecimal precio;
}
