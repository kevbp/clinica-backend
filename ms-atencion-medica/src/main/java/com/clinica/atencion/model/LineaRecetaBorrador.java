package com.clinica.atencion.model;

import lombok.Data;

@Data
public class LineaRecetaBorrador {
    private Long idMedicamento;
    private Integer cantidad;
    private String indicaciones;
}
