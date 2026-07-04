package com.clinica.atencion.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Estructura debe coincidir con lo que ms-historias-clinicas espera deserializar
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EpisodioFinalizadoEvent {
    private Long idCita;
    private Long idPaciente;
    private Long idPersonalMedico;
    private PacienteSnapshotDTO     paciente;
    private MedicoSnapshotDTO       medico;
    private String                  motivoConsulta;
    private SignosVitalesEventDTO   signosVitales;
    private DiagnosticoEventDTO     diagnostico;
    private String                  observacionesClinicas;
    private RecetaEventDTO          receta;
    private OrdenEventDTO           ordenLaboratorio;
}
