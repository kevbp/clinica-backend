package com.clinica.caja.dto;

import com.clinica.caja.model.TipoComprobante;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Comprobante de pago (boleta o factura)")
public class ComprobanteResponseDTO {

    @Schema(description = "ID interno del comprobante", example = "7")
    private Long id;

    @Schema(description = "Tipo de comprobante", example = "PROFORMA")
    private TipoComprobante tipo;

    @Schema(description = "ID del PagoConsulta o Proforma que origina este comprobante", example = "5")
    private Long idOrigen;

    @Schema(description = "Monto total efectivamente cobrado", example = "535.00")
    private BigDecimal montoTotal;

    @Schema(description = "Fecha y hora de emisión", example = "2024-06-15T11:45:00")
    private LocalDateTime fechaEmision;

    @Schema(description = "Número de comprobante", example = "BC-2024-000007")
    private String numero;
}
