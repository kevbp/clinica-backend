package com.clinica.farmacia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Lote de medicamento con su stock actual")
public class LoteResponseDTO {

    @Schema(description = "ID interno del lote", example = "8")
    private Long id;

    @Schema(description = "ID del medicamento al que pertenece", example = "104")
    private Long idMedicamento;

    @Schema(description = "Número de lote del fabricante", example = "LOT-2024-001")
    private String numeroLote;

    @Schema(description = "Fecha de vencimiento", example = "2026-03-31")
    private LocalDate fechaVencimiento;

    @Schema(description = "Cantidad disponible actual", example = "100")
    private Integer cantidadDisponible;
}
