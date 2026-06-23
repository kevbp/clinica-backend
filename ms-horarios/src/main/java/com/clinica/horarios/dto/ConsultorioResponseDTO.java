package com.clinica.horarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Datos de un consultorio físico")
public class ConsultorioResponseDTO {

    @Schema(description = "ID interno del consultorio", example = "1")
    private Long id;

    @Schema(description = "Número o código del consultorio", example = "C-101")
    private String numero;

    @Schema(description = "Piso en el que se encuentra", example = "1")
    private Integer piso;

    @Schema(description = "Descripción adicional de ubicación",
            example = "Ala norte, frente a recepción")
    private String ubicacion;
}
