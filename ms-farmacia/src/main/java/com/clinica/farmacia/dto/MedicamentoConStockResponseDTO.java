package com.clinica.farmacia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Medicamento con stock total y detalle de lotes")
public class MedicamentoConStockResponseDTO {

    @Schema(description = "ID interno del medicamento", example = "104")
    private Long id;

    @Schema(description = "Nombre comercial", example = "Amoxicilina 500mg")
    private String nombre;

    @Schema(description = "Principio activo", example = "Amoxicilina")
    private String principioActivo;

    @Schema(description = "Presentación", example = "Caja x 20 tabletas")
    private String presentacion;

    @Schema(description = "Precio unitario del medicamento", example = "12.50")
    private java.math.BigDecimal precio;

    @Schema(description = "Stock total vigente (suma de lotes no vencidos)", example = "150")
    private Integer stockTotal;

    @Schema(description = "Lotes del medicamento con stock individual")
    private List<LoteResponseDTO> lotes;
}
