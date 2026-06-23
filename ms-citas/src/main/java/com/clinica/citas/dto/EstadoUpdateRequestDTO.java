package com.clinica.citas.dto;

import com.clinica.citas.model.EstadoCita;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Solicitud de cambio de estado de una cita. Invocado por ms-caja (→CONFIRMADA) o ms-atencion-medica (→ATENDIDA).")
public class EstadoUpdateRequestDTO {

    @NotNull
    @Schema(description = "Nuevo estado de la cita",
            example = "CONFIRMADA", requiredMode = Schema.RequiredMode.REQUIRED)
    private EstadoCita estado;
}
