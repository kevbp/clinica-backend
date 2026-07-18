package com.clinica.atencion.model;

import lombok.Data;

@Data
public class LineaOrdenBorrador {
    private Long   idExamen;
    private String nombreExamen;
    private String categoria;
    private String indicacionesPreparacion;
}
