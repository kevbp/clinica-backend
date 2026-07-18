package com.clinica.caja.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "IDs de los ítems seleccionados por el paciente al generar la proforma")
public class ConstruirProformaRequestDTO {

    @NotEmpty(message = "Debe seleccionar al menos un ítem")
    @Schema(description = "Para receta: IDs de idMedicamento. Para orden: IDs de idExamen.",
            example = "[104, 107]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> idsItemsSeleccionados;
}
