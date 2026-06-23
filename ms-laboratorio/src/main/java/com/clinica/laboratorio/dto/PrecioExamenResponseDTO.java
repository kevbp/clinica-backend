package com.clinica.laboratorio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Schema(description = "Precio vigente de un examen. Endpoint exclusivo para ms-caja.")
public class PrecioExamenResponseDTO {

    @Schema(description = "ID del examen", example = "12")
    private Long idExamen;

    @Schema(description = "Precio del examen", example = "45.00")
    private BigDecimal precio;
}
