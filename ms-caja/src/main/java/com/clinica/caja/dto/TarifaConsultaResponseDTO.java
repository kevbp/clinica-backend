package com.clinica.caja.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Tarifa de consulta por especialidad médica")
public class TarifaConsultaResponseDTO {

    @Schema(description = "ID de la especialidad", example = "2")
    private Long idEspecialidad;

    @Schema(description = "Monto de la consulta", example = "80.00")
    private BigDecimal monto;
}
