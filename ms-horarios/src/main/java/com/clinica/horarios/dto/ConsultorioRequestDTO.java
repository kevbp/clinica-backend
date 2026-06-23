package com.clinica.horarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Datos para registrar un consultorio físico")
public class ConsultorioRequestDTO {

    @NotBlank
    @Schema(description = "Número o código identificador del consultorio",
            example = "C-101", requiredMode = Schema.RequiredMode.REQUIRED)
    private String numero;

    @NotNull
    @Schema(description = "Piso en el que se encuentra el consultorio",
            example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer piso;

    @Schema(description = "Descripción adicional de ubicación",
            example = "Ala norte, frente a recepción")
    private String ubicacion;
}
