package com.clinica.laboratorio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Datos para autorizar un examen. Invocado por ms-caja al confirmar el pago del examen.")
public class ExamenAutorizadoRequestDTO {

    @NotNull
    @Schema(description = "ID del paciente (referencia débil a ms-pacientes)",
            example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idPaciente;

    @NotBlank
    @Schema(description = "ID del episodio clínico en ms-historias-clinicas (ObjectId de MongoDB)",
            example = "64a1f3b2e4b0c72a9d8e1f0a", requiredMode = Schema.RequiredMode.REQUIRED)
    private String idEpisodioClinico;

    @NotNull
    @Schema(description = "ID del examen del catálogo",
            example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idExamen;
}
