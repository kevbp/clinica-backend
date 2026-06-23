package com.clinica.historias.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LineaReceta {
    private Long idMedicamento;
    private Integer cantidad;
    private String indicaciones;
}
