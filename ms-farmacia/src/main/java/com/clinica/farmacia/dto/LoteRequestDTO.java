package com.clinica.farmacia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Datos para registrar un lote de medicamento con su stock inicial")
public class LoteRequestDTO {

    @NotBlank
    @Schema(description = "Número de lote del fabricante", example = "LOT-2024-001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String numeroLote;

    @NotNull
    @Schema(description = "Fecha de vencimiento del lote", example = "2026-03-31",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate fechaVencimiento;

    @NotNull
    @Min(1)
    @Schema(description = "Cantidad inicial de unidades en este lote", example = "100",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer cantidadInicial;
}
