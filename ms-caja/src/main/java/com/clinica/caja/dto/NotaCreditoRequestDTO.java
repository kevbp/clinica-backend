package com.clinica.caja.dto;

import com.clinica.caja.model.TipoNotaCredito;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Solicitud de nota de crédito. Invocado por ms-citas al cancelar una cita CONFIRMADA.")
public class NotaCreditoRequestDTO {

    @NotNull
    @Schema(description = "ID de la cita cuya consulta fue pagada y luego cancelada",
            example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idCita;

    @NotBlank
    @Schema(description = "Motivo de la cancelación", example = "Cancelación con anticipación >= 24h",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String motivo;

    @NotNull
    @Schema(description = "Tipo de nota de crédito que determina el porcentaje de devolución",
            example = "CANCELACION_ANTICIPADA", requiredMode = Schema.RequiredMode.REQUIRED)
    private TipoNotaCredito tipo;
}
