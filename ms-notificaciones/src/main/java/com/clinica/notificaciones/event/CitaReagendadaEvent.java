package com.clinica.notificaciones.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CitaReagendadaEvent {
    private Long idCita;
    private Long idPaciente;
    private Long idPersonal;
    private LocalDateTime nuevaFechaHora;
    private String correoPaciente;
    private boolean notificar;
    private String nombrePaciente;
    private String nombreMedico;
    private String especialidad;
}
