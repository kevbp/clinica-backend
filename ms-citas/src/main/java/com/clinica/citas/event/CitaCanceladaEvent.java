package com.clinica.citas.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
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
    // Datos de la nota de credito emitida (null si la cita era PENDIENTE_PAGO, sin pago previo)
    private String numeroNc;
    private BigDecimal montoDevolucion;
    private BigDecimal montoRetenido;
    private String tipoNc;
}