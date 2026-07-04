package com.clinica.horarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Campos actualizables del consultorio. Solo se modifican los campos enviados (no nulos).")
public class ConsultorioUpdateRequestDTO {

    @Schema(description = "Número o código identificador del consultorio", example = "C-101")
    private String numero;

    @Schema(description = "Piso en el que se encuentra el consultorio", example = "1")
    private Integer piso;

    @Schema(description = "Descripción adicional de ubicación", example = "Ala norte, frente a recepción")
    private String ubicacion;
}
