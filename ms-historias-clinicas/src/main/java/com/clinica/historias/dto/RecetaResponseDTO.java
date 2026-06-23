package com.clinica.historias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Receta médica emitida en una atención")
public class RecetaResponseDTO {

    @Schema(description = "ID de la receta (ObjectId MongoDB)", example = "64a1f3b2e4b0c72a9d8e1f0b")
    private String idReceta;

    @Schema(description = "ID del episodio clínico al que pertenece")
    private String idEpisodioClinico;

    @Schema(description = "ID del paciente", example = "42")
    private Long idPaciente;

    @Schema(description = "ID del médico que emitió la receta", example = "5")
    private Long idPersonalMedico;

    @Schema(description = "Líneas de medicamentos prescritos")
    private List<LineaRecetaResponseDTO> lineas;
}
