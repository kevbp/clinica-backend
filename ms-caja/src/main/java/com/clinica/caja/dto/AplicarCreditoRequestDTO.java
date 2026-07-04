package com.clinica.caja.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Solicitud de aplicación de saldo de nota de crédito a un pago")
public class AplicarCreditoRequestDTO {

    @NotNull(message = "El ID de la nota de crédito es obligatorio")
    @Schema(description = "ID de la nota de crédito DISPONIBLE a aplicar", example = "3")
    private Long idNotaCredito;
}
