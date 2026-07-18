package com.clinica.historias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Línea de receta médica")
public class LineaRecetaResponseDTO {

    @Schema(description = "ID del medicamento en ms-farmacia", example = "104")
    private Long idMedicamento;

    @Schema(description = "Nombre comercial del medicamento", example = "Amoxicilina 500mg")
    private String nombreMedicamento;

    @Schema(description = "Principio activo", example = "Amoxicilina")
    private String principioActivo;

    @Schema(description = "Presentación", example = "Cápsula")
    private String presentacion;

    @Schema(description = "Dosis por toma", example = "500 mg")
    private String dosis;

    @Schema(description = "Vía de administración", example = "Oral")
    private String viaAdministracion;

    @Schema(description = "Frecuencia de administración", example = "Cada 8 horas")
    private String frecuencia;

    @Schema(description = "Duración del tratamiento", example = "7 días")
    private String duracion;

    @Schema(description = "Cantidad total de unidades a dispensar", example = "21")
    private Integer cantidadTotal;

    @Schema(description = "Instrucciones adicionales para el paciente")
    private String indicaciones;
}
