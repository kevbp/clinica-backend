package com.clinica.notificaciones.event;

import lombok.Data;

import java.time.LocalDateTime;

// Estructura debe coincidir con lo que ms-atencion-medica publica en atencion.events / episodio.atendido.notificacion
@Data
public class EpisodioAtendidoEvent {
    private Long idCita;
    private String nombrePaciente;
    private String correoPaciente;
    private LocalDateTime fechaHoraAtencion;
    private boolean notificar;
}
