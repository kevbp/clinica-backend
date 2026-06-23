package com.clinica.historias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Diagnóstico CIE-10")
public class DiagnosticoDTO {

    @Schema(description = "Código CIE-10", example = "J18.9")
    private String codigoCie10;

    @Schema(description = "Descripción del diagnóstico", example = "Neumonía no especificada")
    private String descripcion;
}
