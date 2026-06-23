package com.clinica.caja.dto;

import com.clinica.caja.model.EstadoNotaCredito;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Nota de crédito emitida por cancelación dentro de la ventana de 24h")
public class NotaCreditoResponseDTO {

    @Schema(description = "ID de la nota de crédito", example = "3")
    private Long id;

    @Schema(description = "ID del paciente beneficiario", example = "42")
    private Long idPaciente;

    @Schema(description = "Monto acreditado (igual al pago de consulta original)", example = "80.00")
    private BigDecimal monto;

    @Schema(description = "ID del pago de consulta original", example = "10")
    private Long idPagoConsultaOrigen;

    @Schema(description = "Motivo", example = "Cancelación con anticipación ≥ 24h")
    private String motivo;

    @Schema(description = "Estado de la nota de crédito", example = "DISPONIBLE")
    private EstadoNotaCredito estado;
}
