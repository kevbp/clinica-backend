package com.clinica.historias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Línea de receta médica")
public class LineaRecetaResponseDTO {

    @Schema(description = "ID del medicamento en ms-farmacia", example = "104")
    private Long idMedicamento;

    @Schema(description = "Cantidad prescrita", example = "20")
    private Integer cantidad;

    @Schema(description = "Indicaciones de administración", example = "1 tableta cada 8 horas por 7 días")
    private String indicaciones;
}
