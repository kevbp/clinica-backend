package com.clinica.historias.event.dto;

import lombok.Data;

@Data
public class LineaRecetaEventDTO {
    private Long idMedicamento;
    private Integer cantidad;
    private String indicaciones;
}
