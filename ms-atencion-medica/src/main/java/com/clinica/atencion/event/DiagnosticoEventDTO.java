package com.clinica.atencion.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiagnosticoEventDTO {
    private String codigoCie10;
    private String descripcion;
}
