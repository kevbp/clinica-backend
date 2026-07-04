package com.clinica.caja.dto;

import com.clinica.caja.model.EstadoNotaCredito;
import com.clinica.caja.model.TipoNotaCredito;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Nota de crédito emitida al cancelar una cita CONFIRMADA")
public class NotaCreditoResponseDTO {

    @Schema(description = "ID de la nota de crédito", example = "3")
    private Long id;

    @Schema(description = "Número formal (SUNAT) de la nota de crédito", example = "NC-20260703-00003")
    private String numero;

    @Schema(description = "ID del paciente beneficiario", example = "42")
    private Long idPaciente;

    @Schema(description = "Tipo de nota de crédito (determina el porcentaje devuelto)", example = "CANCELACION_ANTICIPADA")
    private TipoNotaCredito tipo;

    @Schema(description = "Monto devuelto al paciente (70 % o 100 % según tipo)", example = "80.00")
    private BigDecimal monto;

    @Schema(description = "Monto retenido como penalidad (30 % en cancelación tardía / no-show; 0 en los demás)", example = "34.20")
    private BigDecimal montoRetenido;

    @Schema(description = "ID del pago de consulta original", example = "10")
    private Long idPagoConsultaOrigen;

    @Schema(description = "Motivo", example = "Cancelación con anticipación ≥ 24h")
    private String motivo;

    @Schema(description = "Estado de la nota de crédito", example = "DISPONIBLE")
    private EstadoNotaCredito estado;

    @Schema(description = "ID del comprobante original que esta NC modifica", example = "5")
    private Long idComprobanteRelacionado;
}
