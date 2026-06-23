package com.clinica.pacientes.dto;

import com.clinica.pacientes.model.TipoAntecedente;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Antecedente clínico o alergia de un paciente")
public class AntecedenteClinicoResponseDTO {

    @Schema(description = "ID interno del antecedente", example = "7")
    private Long id;

    @Schema(description = "ID del paciente al que pertenece", example = "42")
    private Long idPaciente;

    @Schema(description = "Descripción del antecedente o alergia",
            example = "Alergia a la penicilina")
    private String descripcion;

    @Schema(description = "Tipo de antecedente", example = "ALERGIA")
    private TipoAntecedente tipo;
}
