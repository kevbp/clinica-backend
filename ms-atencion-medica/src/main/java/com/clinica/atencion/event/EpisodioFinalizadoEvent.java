package com.clinica.atencion.event;

import lombok.AllArgsConstructor;
import lombok.Data;

// Estructura debe coincidir con lo que ms-historias-clinicas espera deserializar
@Data
@AllArgsConstructor
public class EpisodioFinalizadoEvent {
    private Long idCita;
    private Long idPaciente;
    private Long idPersonalMedico;
    private DiagnosticoEventDTO diagnostico;
    private String observacionesClinicas;
    private RecetaEventDTO receta;          // null si no hubo prescripción
    private OrdenEventDTO ordenLaboratorio;  // null si no hubo orden de examen
}
