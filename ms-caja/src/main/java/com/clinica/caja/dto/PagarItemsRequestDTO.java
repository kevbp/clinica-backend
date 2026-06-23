package com.clinica.caja.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Lista de IDs de ítems a pagar en esta liquidación (parcial o total)")
public class PagarItemsRequestDTO {

    @NotEmpty
    @Schema(description = "IDs de los ItemProforma a pagar", example = "[20, 21]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> idsItems;
}
