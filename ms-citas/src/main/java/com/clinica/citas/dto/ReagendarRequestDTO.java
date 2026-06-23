package com.clinica.citas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Solicitud de reagendamiento de una cita CONFIRMADA")
public class ReagendarRequestDTO {

    @NotNull
    @Future
    @Schema(description = "Nueva fecha y hora deseada (debe ser futura y coincidir con un bloque disponible)",
            example = "2024-07-15T11:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime nuevaFechaHora;
}
