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
}
