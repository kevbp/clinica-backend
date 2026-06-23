package com.clinica.citas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description = "Bloque de 20 minutos disponible para agendar una cita")
public class SlotDisponibleDTO {

    @Schema(description = "Fecha y hora de inicio del bloque disponible", example = "2024-07-10T09:00:00")
    private LocalDateTime fechaHora;

    @Schema(description = "ID del consultorio en el que se atendería", example = "1")
    private Long idConsultorio;
}
