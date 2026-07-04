package com.clinica.caja.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagoConsultaConfirmadoEvent {
    private Long idCita;
    private Long idPaciente;
    private BigDecimal monto;
    private String correoPaciente;
    private String nombrePaciente;
    // Datos de la boleta para el email automatico
    private String numeroBoleta;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal montoTotal;
    private BigDecimal descuento;
    private String conceptoDescuento;
    private LocalDateTime fechaEmision;
    private String especialidad;
}