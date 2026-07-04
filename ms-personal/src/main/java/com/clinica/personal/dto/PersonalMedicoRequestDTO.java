package com.clinica.personal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Datos para registrar la extensión médica. El ID del personal se indica en la URL.")
public class PersonalMedicoRequestDTO {

    @NotBlank
    @Schema(description = "Número de colegiatura médica (CMP)",
            example = "CMP-12345", requiredMode = Schema.RequiredMode.REQUIRED)
    private String numeroColegiatura;

    @NotNull
    @Schema(description = "ID de la especialidad médica",
            example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idEspecialidad;
}
