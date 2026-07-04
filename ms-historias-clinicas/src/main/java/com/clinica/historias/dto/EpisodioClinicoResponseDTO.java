package com.clinica.historias.dto;

import com.clinica.historias.model.MedicoSnapshot;
import com.clinica.historias.model.PacienteSnapshot;
import com.clinica.historias.model.SignosVitales;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Vista de lista de un episodio clínico (sin receta, orden ni adendas)")
public class EpisodioClinicoResponseDTO {

    @Schema(description = "ID del episodio (ObjectId MongoDB)", example = "64a1f3b2e4b0c72a9d8e1f0a")
    private String idEpisodio;

    @Schema(description = "ID de la historia clínica a la que pertenece este episodio")
    private String idHistoriaClinica;

    @Schema(description = "ID del paciente", example = "42")
    private Long idPaciente;

    @Schema(description = "ID de la cita asociada", example = "100")
    private Long idCita;

    @Schema(description = "ID del médico que atendió", example = "5")
    private Long idPersonalMedico;

    @Schema(description = "Snapshot del paciente al momento de la atención")
    private PacienteSnapshot paciente;

    @Schema(description = "Snapshot del médico al momento de la atención")
    private MedicoSnapshot medico;

    @Schema(description = "Fecha y hora de la atención")
    private LocalDateTime fechaAtencion;

    @Schema(description = "Motivo de consulta (Subjetivo)")
    private String motivoConsulta;

    @Schema(description = "Signos vitales registrados")
    private SignosVitales signosVitales;

    @Schema(description = "Diagnóstico CIE-10")
    private DiagnosticoDTO diagnostico;

    @Schema(description = "Observaciones clínicas")
    private String observacionesClinicas;
}
