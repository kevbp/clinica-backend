package com.clinica.caja.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReenviarComprobanteEvent {
    private Long idComprobante;
    private String numero;
    private String correoDestino;
    private BigDecimal montoTotal;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private LocalDateTime fechaEmision;
}
