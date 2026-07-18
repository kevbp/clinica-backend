package com.clinica.historias.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LineaOrden {
    private Long   idExamen;
    private String nombreExamen;
    private String categoria;
    private String indicacionesPreparacion;
}
