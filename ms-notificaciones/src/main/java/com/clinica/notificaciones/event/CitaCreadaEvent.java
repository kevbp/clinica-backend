package com.clinica.notificaciones.event;

import lombok.Data;

import java.time.LocalDateTime;

// Estructura debe coincidir con lo que ms-citas publica en citas.events / cita.creada
@Data
public class CitaCreadaEvent {
    private Long idCita;
    private Long idPaciente;
    private Long idPersonal;
    private Long idConsultorio;
    private LocalDateTime fechaHora;
    private String correoPaciente;
}
