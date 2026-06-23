package com.clinica.atencion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Línea de medicamento a agregar a la receta del borrador")
public class AgregarRecetaRequestDTO {

    @NotNull
    @Schema(description = "ID del medicamento en ms-farmacia", example = "104",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idMedicamento;

    @NotNull
    @Min(1)
    @Schema(description = "Cantidad a prescribir", example = "20",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer cantidad;

    @NotBlank
    @Schema(description = "Indicaciones de administración",
            example = "1 tableta cada 8 horas durante 7 días",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String indicaciones;
}
