package com.clinica.caja.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RetiroSolicitadoEvent {
    private Long idRetiro;
    private Long idPaciente;
    private String nombreTitular;
    private String correoDestino;
    private BigDecimal monto;
    private String nombreBanco;
    private String numeroCuenta;
    private LocalDateTime fechaSolicitud;
}
