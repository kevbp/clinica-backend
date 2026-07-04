package com.clinica.notificaciones.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ReenviarNotaCreditoEvent {
    private Long idNotaCredito;
    private String numero;
    private String correoDestino;
    private String tipo;
    private BigDecimal monto;
    private BigDecimal montoRetenido;
    private String motivo;
    private LocalDateTime fechaEmision;
}
