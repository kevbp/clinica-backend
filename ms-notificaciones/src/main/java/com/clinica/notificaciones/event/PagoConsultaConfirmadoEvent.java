package com.clinica.notificaciones.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Estructura debe coincidir con lo que ms-caja publica en caja.events / pago-consulta.confirmado
@Data
@NoArgsConstructor
public class PagoConsultaConfirmadoEvent {
    private Long idCita;
    private Long idPaciente;
    private BigDecimal monto;
    private String correoPaciente;
    private String nombrePaciente;
    // Datos de la boleta
    private String numeroBoleta;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal montoTotal;
    private BigDecimal descuento;
    private String conceptoDescuento;
    private LocalDateTime fechaEmision;
    private String especialidad;
}