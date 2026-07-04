package com.clinica.citas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Datos para crear una cita médica")
public class CitaMedicaRequestDTO {

    @NotNull
    @Schema(description = "ID del paciente (referencia débil a ms-pacientes)",
            example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idPaciente;

    @NotNull
    @Schema(description = "ID del médico (referencia débil a ms-personal)",
            example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idPersonal;

    @NotNull
    @Future
    @Schema(description = "Fecha y hora deseada para la cita (debe ser futura y coincidir con un bloque de 20 min disponible)",
            example = "2024-07-10T09:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime fechaHora;

    @Schema(description = "Si se debe notificar por correo al paciente al agendar (vía evento CitaCreada). Por defecto true.",
            example = "true", defaultValue = "true")
    private Boolean notificarCorreo = true;
}
