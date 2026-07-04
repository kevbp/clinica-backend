package com.clinica.citas.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
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
