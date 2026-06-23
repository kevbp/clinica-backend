package com.clinica.historias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Vista de lista de un episodio clínico (sin receta, orden ni adendas)")
public class EpisodioClinicoResponseDTO {

    @Schema(description = "ID del episodio (ObjectId MongoDB)", example = "64a1f3b2e4b0c72a9d8e1f0a")
    private String idEpisodio;

    @Schema(description = "ID del paciente", example = "42")
    private Long idPaciente;

    @Schema(description = "ID de la cita asociada", example = "100")
    private Long idCita;

    @Schema(description = "ID del médico que atendió", example = "5")
    private Long idPersonalMedico;

    @Schema(description = "Fecha y hora de la atención")
    private LocalDateTime fechaAtencion;

    @Schema(description = "Diagnóstico CIE-10")
    private DiagnosticoDTO diagnostico;

    @Schema(description = "Observaciones clínicas")
    private String observacionesClinicas;
}
