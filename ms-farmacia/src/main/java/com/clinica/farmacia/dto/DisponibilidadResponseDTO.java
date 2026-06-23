package com.clinica.farmacia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Stock teórico agregado de un medicamento en lotes vigentes. Solo lectura — nunca descuenta.")
public class DisponibilidadResponseDTO {

    @Schema(description = "ID del medicamento", example = "104")
    private Long idMedicamento;

    @Schema(description = "Cantidad total disponible en lotes no vencidos", example = "320")
    private Integer cantidadTotal;
}
