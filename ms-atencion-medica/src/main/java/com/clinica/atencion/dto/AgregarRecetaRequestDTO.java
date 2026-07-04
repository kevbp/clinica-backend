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

    @NotBlank
    @Schema(description = "Dosis por toma (cantidad + unidad)", example = "500 mg",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String dosis;

    @NotBlank
    @Schema(description = "Vía de administración", example = "Oral",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String viaAdministracion;

    @NotBlank
    @Schema(description = "Frecuencia de administración", example = "Cada 8 horas",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String frecuencia;

    @NotBlank
    @Schema(description = "Duración del tratamiento", example = "7 días",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String duracion;

    @NotNull
    @Min(1)
    @Schema(description = "Cantidad total a dispensar", example = "21",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer cantidadTotal;

    @Schema(description = "Indicaciones adicionales para el paciente",
            example = "Tomar después de los alimentos")
    private String indicaciones;
}
