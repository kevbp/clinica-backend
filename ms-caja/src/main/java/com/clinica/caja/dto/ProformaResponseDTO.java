package com.clinica.caja.dto;

import com.clinica.caja.model.EstadoProforma;
import com.clinica.caja.model.TipoProforma;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Proforma de cobro vinculada a una receta o una orden de laboratorio")
public class ProformaResponseDTO {

    @Schema(description = "ID de la proforma", example = "5")
    private Long id;

    @Schema(description = "ID del paciente", example = "42")
    private Long idPaciente;

    @Schema(description = "ObjectId MongoDB de la receta de origen (null si tipo=EXAMENES)")
    private String idReceta;

    @Schema(description = "ObjectId MongoDB de la orden de laboratorio de origen (null si tipo=MEDICAMENTOS)")
    private String idOrden;

    @Schema(description = "Tipo de proforma", example = "MEDICAMENTOS")
    private TipoProforma tipo;

    @Schema(description = "Fecha de generación de la proforma")
    private LocalDateTime fechaGeneracion;

    @Schema(description = "Fecha de vencimiento de la proforma (7 días después de fechaGeneracion)")
    private LocalDateTime fechaVigencia;

    @Schema(description = "Estado calculado de la proforma: VIGENTE, EXPIRADA o PAGADA")
    private EstadoProforma estadoProforma;

    @Schema(description = "Ítems con precios congelados")
    private List<ItemProformaResponseDTO> items;
}
