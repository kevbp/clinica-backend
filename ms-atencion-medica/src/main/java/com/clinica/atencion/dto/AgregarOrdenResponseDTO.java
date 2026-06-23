package com.clinica.atencion.dto;

import com.clinica.atencion.client.dto.ExamenCatalogoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Resultado de agregar una línea de orden de laboratorio")
public class AgregarOrdenResponseDTO {

    @Schema(description = "Borrador actualizado")
    private BorradorResponseDTO borrador;

    @Schema(description = "Detalle del examen del catálogo. Sin precio.")
    private ExamenCatalogoDTO detalleExamen;
}
