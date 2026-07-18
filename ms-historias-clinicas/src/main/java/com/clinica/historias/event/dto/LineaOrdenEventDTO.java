package com.clinica.historias.event.dto;

import lombok.Data;

@Data
public class LineaOrdenEventDTO {
    private Long   idExamen;
    private String nombreExamen;
    private String categoria;
    private String indicacionesPreparacion;
}
