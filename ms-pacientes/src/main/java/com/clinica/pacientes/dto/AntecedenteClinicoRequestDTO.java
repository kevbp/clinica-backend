package com.clinica.pacientes.dto;

import com.clinica.pacientes.model.TipoAntecedente;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Datos para registrar un antecedente clínico o alergia")
public class AntecedenteClinicoRequestDTO {

    @NotBlank
    @Schema(description = "Descripción del antecedente o alergia",
            example = "Alergia a la penicilina", requiredMode = Schema.RequiredMode.REQUIRED)
    private String descripcion;

    @NotNull
    @Schema(description = "Tipo de antecedente",
            example = "ALERGIA", requiredMode = Schema.RequiredMode.REQUIRED)
    private TipoAntecedente tipo;
}
