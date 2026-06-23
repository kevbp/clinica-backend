package com.clinica.historias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Solicitud de adenda clínica. Solo el médico autor del episodio puede registrarla.")
public class AdendaClinicoRequestDTO {

    @NotBlank
    @Schema(description = "ID del episodio clínico original (ObjectId MongoDB)",
            example = "64a1f3b2e4b0c72a9d8e1f0a", requiredMode = Schema.RequiredMode.REQUIRED)
    private String idEpisodioPadre;

    @NotNull
    @Schema(description = "ID del médico que solicita la adenda (debe coincidir con el autor del episodio)",
            example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idPersonalMedico;

    @NotBlank
    @Schema(description = "Texto de rectificación o aclaración",
            example = "Corrección: el diagnóstico correcto es J18.1, no J18.9",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String textoRectificacion;
}
