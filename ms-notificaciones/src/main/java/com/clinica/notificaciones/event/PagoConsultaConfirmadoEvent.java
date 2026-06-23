package com.clinica.notificaciones.event;

import lombok.Data;

import java.math.BigDecimal;

// Estructura debe coincidir con lo que ms-caja publica en caja.events / pago-consulta.confirmado
@Data
public class PagoConsultaConfirmadoEvent {
    private Long idCita;
    private Long idPaciente;
    private BigDecimal monto;
    private String correoPaciente;
}
