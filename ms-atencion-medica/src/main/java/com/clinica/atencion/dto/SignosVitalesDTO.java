package com.clinica.atencion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Signos vitales tomados durante la consulta")
public class SignosVitalesDTO {

    @Schema(description = "Peso en kilogramos", example = "70.5")
    private Double peso;

    @Schema(description = "Talla en centímetros", example = "168")
    private Double talla;

    @Schema(description = "Presión arterial (sistólica/diastólica)", example = "120/80")
    private String presionArterial;

    @Schema(description = "Frecuencia cardíaca en latidos por minuto", example = "72")
    private Integer frecuenciaCardiaca;

    @Schema(description = "Temperatura corporal en grados Celsius", example = "37.0")
    private Double temperatura;

    @Schema(description = "Saturación de oxígeno en porcentaje", example = "98")
    private Integer saturacionOxigeno;

    @Schema(description = "Frecuencia respiratoria en respiraciones por minuto", example = "16")
    private Integer frecuenciaRespiratoria;

    @Schema(description = "Índice de masa corporal (calculado: peso / (talla/100)²)", example = "24.97")
    private Double imc;
}
