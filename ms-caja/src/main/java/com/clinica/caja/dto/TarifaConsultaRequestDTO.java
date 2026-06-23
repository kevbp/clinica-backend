package com.clinica.caja.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Datos para registrar o actualizar la tarifa de una especialidad médica")
public class TarifaConsultaRequestDTO {

    @NotNull
    @Schema(description = "ID de la especialidad en ms-personal", example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idEspecialidad;

    @NotNull
    @DecimalMin("0.01")
    @Schema(description = "Monto de la consulta para esa especialidad", example = "80.00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal monto;
}
