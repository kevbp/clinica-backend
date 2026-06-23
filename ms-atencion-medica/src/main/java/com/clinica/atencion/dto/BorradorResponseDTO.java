package com.clinica.atencion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Estado actual del borrador de atención médica en Redis")
public class BorradorResponseDTO {

    @Schema(description = "ID de la cita a la que pertenece el borrador", example = "100")
    private Long idCita;

    @Schema(description = "ID del paciente", example = "42")
    private Long idPaciente;

    @Schema(description = "ID del médico", example = "5")
    private Long idPersonalMedico;

    @Schema(description = "Diagnóstico CIE-10 (null si aún no se ha ingresado)")
    private DiagnosticoDTO diagnostico;

    @Schema(description = "Observaciones clínicas libres")
    private String observacionesClinicas;

    @Schema(description = "Líneas de receta agregadas hasta el momento")
    private List<LineaRecetaDTO> lineasReceta;

    @Schema(description = "Líneas de orden de laboratorio agregadas hasta el momento")
    private List<LineaOrdenDTO> lineasOrden;
}
