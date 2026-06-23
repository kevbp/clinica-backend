package com.clinica.historias.event.dto;

import lombok.Data;

@Data
public class EpisodioFinalizadoEvent {
    private Long idCita;
    private Long idPaciente;
    private Long idPersonalMedico;
    private DiagnosticoEventDTO diagnostico;
    private String observacionesClinicas;
    private RecetaEventDTO receta;         // null si no se emitió receta
    private OrdenEventDTO ordenLaboratorio; // null si no se ordenó laboratorio
}
