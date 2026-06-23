package com.clinica.personal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Datos de una especialidad médica")
public class EspecialidadResponseDTO {

    @Schema(description = "ID interno de la especialidad", example = "1")
    private Long id;

    @Schema(description = "Nombre de la especialidad", example = "Cardiología")
    private String nombre;

    @Schema(description = "Descripción de la especialidad",
            example = "Especialidad del corazón y sistema circulatorio")
    private String descripcion;
}
