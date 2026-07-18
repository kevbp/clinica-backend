package com.clinica.caja.client.dto;

import lombok.Data;

@Data
public class LineaOrdenDTO {
    private Long   idExamen;
    private String nombreExamen;
    private String categoria;
    private String indicacionesPreparacion;
}
