package com.clinica.atencion.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DiagnosticoBorrador {
    private String codigoCie10;
    private String descripcion;
    /** PRESUNTIVO | DEFINITIVO */
    private String tipoDiagnostico = "PRESUNTIVO";
}
