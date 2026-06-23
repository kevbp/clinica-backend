package com.clinica.personal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Datos para crear una especialidad médica")
public class EspecialidadRequestDTO {

    @NotBlank
    @Schema(description = "Nombre de la especialidad", example = "Cardiología",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @Schema(description = "Descripción de la especialidad",
            example = "Especialidad del corazón y sistema circulatorio")
    private String descripcion;
}
