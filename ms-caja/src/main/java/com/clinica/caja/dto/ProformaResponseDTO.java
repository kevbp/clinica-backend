package com.clinica.caja.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Proforma de cobro de receta y exámenes post-atención médica")
public class ProformaResponseDTO {

    @Schema(description = "ID de la proforma", example = "5")
    private Long id;

    @Schema(description = "ID del paciente", example = "42")
    private Long idPaciente;

    @Schema(description = "Fecha de generación", example = "2024-06-15T11:30:00")
    private LocalDateTime fechaGeneracion;

    @Schema(description = "Ítems con precios congelados")
    private List<ItemProformaResponseDTO> items;
}
