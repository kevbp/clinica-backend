package com.clinica.personal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Extensión médica de un miembro del personal")
public class PersonalMedicoResponseDTO {

    @Schema(description = "ID del Personal al que pertenece esta extensión", example = "5")
    private Long idPersonal;

    @Schema(description = "Nombres del médico", example = "Carlos Alberto")
    private String nombres;

    @Schema(description = "Apellidos del médico", example = "Ramírez Soto")
    private String apellidos;

    @Schema(description = "Número de colegiatura médica (CMP)", example = "CMP-12345")
    private String numeroColegiatura;

    @Schema(description = "Especialidad asignada al médico")
    private EspecialidadResponseDTO especialidad;
}
