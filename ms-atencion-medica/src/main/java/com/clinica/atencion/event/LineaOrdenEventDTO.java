package com.clinica.atencion.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LineaOrdenEventDTO {
    private Long idExamen;
    private String indicacionesPreparacion;
}
