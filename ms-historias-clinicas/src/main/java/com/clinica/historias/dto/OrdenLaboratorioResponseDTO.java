package com.clinica.historias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Orden de laboratorio emitida en una atención")
public class OrdenLaboratorioResponseDTO {

    @Schema(description = "ID de la orden (ObjectId MongoDB)", example = "64a1f3b2e4b0c72a9d8e1f0c")
    private String idOrden;

    @Schema(description = "ID del episodio clínico al que pertenece")
    private String idEpisodioClinico;

    @Schema(description = "ID del paciente", example = "42")
    private Long idPaciente;

    @Schema(description = "ID del médico que emitió la orden", example = "5")
    private Long idPersonalMedico;

    @Schema(description = "Líneas de exámenes ordenados")
    private List<LineaOrdenResponseDTO> lineas;
}
