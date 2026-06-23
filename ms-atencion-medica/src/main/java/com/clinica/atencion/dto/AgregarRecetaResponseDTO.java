package com.clinica.atencion.dto;

import com.clinica.atencion.client.dto.AntecedenteClinicoDTO;
import com.clinica.atencion.client.dto.DisponibilidadDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Resultado de agregar una línea de receta, incluyendo advertencias al médico")
public class AgregarRecetaResponseDTO {

    @Schema(description = "Borrador actualizado")
    private BorradorResponseDTO borrador;

    @Schema(description = "Alergias y antecedentes del paciente relevantes para la decisión clínica. " +
                          "El médico es responsable de la decisión final.")
    private List<AntecedenteClinicoDTO> advertenciasAntecedentes;

    @Schema(description = "Disponibilidad de stock del medicamento recién prescrito. Solo lectura — no descuenta.")
    private DisponibilidadDTO disponibilidadMedicamento;
}
