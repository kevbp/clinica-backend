package com.clinica.atencion.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosticoEventDTO {
    private String codigoCie10;
    private String descripcion;
    private String tipoDiagnostico;
}
