package com.clinica.atencion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Diagnóstico a agregar al borrador (sección A del SOAP)")
public class DiagnosticoRequestDTO {

    @NotBlank
    @Pattern(regexp = "^[A-Z][0-9]{2}(\\.[0-9A-Z]{1,4})?$",
             message = "Código CIE-10 inválido. Formato esperado: letra + 2 dígitos + subcódigo opcional (ej. J18.9)")
    @Schema(description = "Código CIE-10", example = "J18.9",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String codigoCie10;

    @NotBlank
    @Schema(description = "Descripción clínica del diagnóstico",
            example = "Neumonía no especificada", requiredMode = Schema.RequiredMode.REQUIRED)
    private String descripcion;

    @Schema(description = "Tipo de diagnóstico", example = "PRESUNTIVO",
            allowableValues = {"PRESUNTIVO", "DEFINITIVO"})
    private String tipoDiagnostico = "PRESUNTIVO";

    @Schema(description = "Observaciones clínicas adicionales del médico",
            example = "Paciente con fiebre 38.5°C, tos productiva")
    private String observacionesClinicas;
}
