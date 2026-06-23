package com.clinica.caja.dto;

import com.clinica.caja.model.EstadoItem;
import com.clinica.caja.model.TipoItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Ítem de una proforma con precio congelado")
public class ItemProformaResponseDTO {

    @Schema(description = "ID del ítem", example = "20")
    private Long id;

    @Schema(description = "Tipo de ítem", example = "MEDICAMENTO")
    private TipoItem tipo;

    @Schema(description = "ID del medicamento o examen de referencia", example = "104")
    private Long idItem;

    @Schema(description = "Descripción del ítem", example = "Amoxicilina 500mg")
    private String descripcion;

    @Schema(description = "Precio congelado al momento de generar la proforma", example = "25.50")
    private BigDecimal precioCongelado;

    @Schema(description = "Cantidad (solo para MEDICAMENTO)", example = "20")
    private Integer cantidad;

    @Schema(description = "Estado del ítem", example = "PENDIENTE")
    private EstadoItem estado;
}
