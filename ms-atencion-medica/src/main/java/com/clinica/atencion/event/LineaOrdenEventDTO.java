package com.clinica.atencion.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LineaOrdenEventDTO {
    private Long   idExamen;
    private String nombreExamen;
    private String categoria;
    private String indicacionesPreparacion;
}
