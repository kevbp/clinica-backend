package com.clinica.notificaciones.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Estructura debe coincidir con lo que ms-citas publica en citas.events / cita.cancelada
@Data
@NoArgsConstructor
public class CitaCanceladaEvent {
    private Long idCita;
    private Long idPaciente;
    private LocalDateTime fechaHora;
    private String correoPaciente;
    private boolean notificar;
    private String nombrePaciente;
    private String nombreMedico;
    private String especialidad;
    private String motivo;
    // Datos de la nota de credito emitida (null si la cita era PENDIENTE_PAGO)
    private String numeroNc;
    private BigDecimal montoDevolucion;
    private BigDecimal montoRetenido;
    private String tipoNc;
}