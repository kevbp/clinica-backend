package com.clinica.personal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Datos para registrar la extensión médica de un Personal de tipo MEDICO")
public class PersonalMedicoRequestDTO {

    @NotNull
    @Schema(description = "ID del Personal existente (debe tener tipoPersonal = MEDICO)",
            example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idPersonal;

    @NotBlank
    @Schema(description = "Número de colegiatura médica (CMP)",
            example = "CMP-12345", requiredMode = Schema.RequiredMode.REQUIRED)
    private String numeroColegiatura;

    @NotNull
    @Schema(description = "ID de la especialidad médica",
            example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idEspecialidad;
}
