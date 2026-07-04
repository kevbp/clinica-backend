package com.clinica.atencion.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

// Evento liviano exclusivo para ms-notificaciones (no para persistencia del EHR).
// Todo dato necesario para el correo va embebido aquí: ms-notificaciones nunca hace Feign.
@Data
@AllArgsConstructor
public class EpisodioAtendidoEvent {
    private Long idCita;
    private String nombrePaciente;
    private String correoPaciente;
    private LocalDateTime fechaHoraAtencion;
    private boolean notificar;
}
