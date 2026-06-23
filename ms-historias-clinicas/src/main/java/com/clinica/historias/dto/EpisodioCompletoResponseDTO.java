package com.clinica.historias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Vista completa de un episodio clínico: datos base + receta + orden de laboratorio + adendas. " +
        "El frontend genera la vista imprimible/PDF a partir de este JSON.")
public class EpisodioCompletoResponseDTO {

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

    @Schema(description = "Receta emitida (null si no hubo prescripción)")
    private RecetaResponseDTO receta;

    @Schema(description = "Orden de laboratorio emitida (null si no hubo indicación)")
    private OrdenLaboratorioResponseDTO ordenLaboratorio;

    @Schema(description = "Adendas registradas sobre este episodio, en orden cronológico")
    private List<AdendaClinicoResponseDTO> adendas;
}
