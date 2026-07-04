package com.clinica.citas.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CitaCreadaEvent {
    private Long idCita;
    private Long idPaciente;
    private Long idPersonal;
    private Long idConsultorio;
    private LocalDateTime fechaHora;
    private String correoPaciente; // contacto del paciente, usado por ms-notificaciones
    private boolean notificar; // si es false, ms-notificaciones omite el envío del correo
    private String nombrePaciente;
    private String nombreMedico;
    private String especialidad; // null si el médico no tiene especialidad asignada
}
