package com.clinica.atencion.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LineaRecetaEventDTO {
    private Long idMedicamento;
    private Integer cantidad;
    private String indicaciones;
}
