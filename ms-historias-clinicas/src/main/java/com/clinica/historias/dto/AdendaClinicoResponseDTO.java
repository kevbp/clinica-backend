package com.clinica.historias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Adenda clínica registrada sobre un episodio")
public class AdendaClinicoResponseDTO {

    @Schema(description = "ID de la adenda (ObjectId MongoDB)")
    private String idAdenda;

    @Schema(description = "ID del episodio clínico al que rectifica")
    private String idEpisodioPadre;

    @Schema(description = "Timestamp exacto del servidor en que se registró la adenda")
    private LocalDateTime fechaCorreccion;

    @Schema(description = "ID del médico autor de la adenda", example = "5")
    private Long idPersonalMedico;

    @Schema(description = "Texto de rectificación",
            example = "Corrección: el diagnóstico correcto es J18.1, no J18.9")
    private String textoRectificacion;
}
