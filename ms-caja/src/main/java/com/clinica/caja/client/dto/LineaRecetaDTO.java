package com.clinica.caja.client.dto;

import lombok.Data;

@Data
public class LineaRecetaDTO {
    private Long idMedicamento;
    private Integer cantidad;
    private String indicaciones;
}
