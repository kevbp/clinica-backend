package com.clinica.historias.event.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
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

    @Data
    public static class PacienteSnapshotDTO {
        private Long      id;
        private String    nombres;
        private String    apellidos;
        private String    documentoIdentidad;
        private LocalDate fechaNacimiento;
    }

    @Data
    public static class MedicoSnapshotDTO {
        private Long   id;
        private String nombres;
        private String apellidos;
        private String numeroColegiatura;
        private String especialidad;
    }
}
