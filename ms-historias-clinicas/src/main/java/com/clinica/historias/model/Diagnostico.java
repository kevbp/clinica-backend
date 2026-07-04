package com.clinica.historias.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Diagnostico {
    private String codigoCie10;
    private String descripcion;
    /** PRESUNTIVO | DEFINITIVO */
    private String tipoDiagnostico;
}
