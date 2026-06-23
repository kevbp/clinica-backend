package com.clinica.atencion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Datos para iniciar una atención médica. La cita debe estar en estado CONFIRMADA.")
public class IniciarAtencionRequestDTO {

    @NotNull
    @Schema(description = "ID de la cita CONFIRMADA", example = "100",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idCita;

    @NotNull
    @Schema(description = "ID del paciente", example = "42",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idPaciente;

    @NotNull
    @Schema(description = "ID del médico que atiende", example = "5",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idPersonalMedico;
}
