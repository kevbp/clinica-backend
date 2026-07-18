package com.clinica.caja.dto;

import com.clinica.caja.model.EstadoItem;
import com.clinica.caja.model.TipoItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Ítem de una proforma con precio congelado y datos clínicos completos")
public class ItemProformaResponseDTO {

    @Schema(description = "ID del ítem", example = "20")
    private Long id;

    @Schema(description = "Tipo de ítem", example = "MEDICAMENTO")
    private TipoItem tipo;

    @Schema(description = "ID del medicamento o examen de referencia", example = "104")
    private Long idItem;

    @Schema(description = "Nombre del medicamento o examen (dato duro)", example = "Amoxicilina")
    private String nombreItem;

    @Schema(description = "Principio activo (solo MEDICAMENTO)", example = "Amoxicilina trihidrato")
    private String principioActivo;

    @Schema(description = "Presentación del medicamento (solo MEDICAMENTO)", example = "Cápsula 500mg")
    private String presentacion;

    @Schema(description = "Dosis prescrita (solo MEDICAMENTO)", example = "1 cápsula")
    private String dosis;

    @Schema(description = "Frecuencia de administración (solo MEDICAMENTO)", example = "Cada 8 horas")
    private String frecuencia;

    @Schema(description = "Duración del tratamiento (solo MEDICAMENTO)", example = "7 días")
    private String duracion;

    @Schema(description = "Categoría del examen (solo EXAMEN)", example = "Hematología")
    private String categoria;

    @Schema(description = "Indicaciones de preparación (solo EXAMEN)", example = "Ayunas 8 horas")
    private String indicacionesPreparacion;

    @Schema(description = "Precio unitario congelado", example = "5.50")
    private BigDecimal precioUnitario;

    @Schema(description = "Precio total congelado (precioUnitario x cantidad para MEDICAMENTO)", example = "110.00")
    private BigDecimal precioCongelado;

    @Schema(description = "Cantidad (solo MEDICAMENTO)", example = "20")
    private Integer cantidad;

    @Schema(description = "Estado del ítem", example = "PENDIENTE")
    private EstadoItem estado;
}
