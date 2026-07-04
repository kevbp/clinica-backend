package com.clinica.atencion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Anamnesis y signos vitales del paciente (secciones S y O del SOAP)")
public class ActualizarAnamnesisRequestDTO {

    @Schema(description = "Motivo de consulta relatado por el paciente",
            example = "Dolor de garganta y fiebre desde hace 2 días")
    private String motivoConsulta;

    @Schema(description = "Signos vitales medidos al inicio de la consulta")
    private SignosVitalesDTO signosVitales;
}
