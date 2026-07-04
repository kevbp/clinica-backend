package com.clinica.notificaciones.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
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
