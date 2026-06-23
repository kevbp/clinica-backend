package com.clinica.farmacia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Solicitud de descuento de stock. Invocado únicamente por ms-caja al confirmar pago.")
public class DescontarStockRequestDTO {

    @NotNull
    @Min(1)
    @Schema(description = "Cantidad de unidades a descontar", example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer cantidad;
}
