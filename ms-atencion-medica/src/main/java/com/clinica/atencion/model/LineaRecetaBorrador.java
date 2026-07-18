package com.clinica.atencion.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LineaRecetaBorrador {
    private Long    idMedicamento;
    private String  nombreMedicamento;
    private String  principioActivo;
    private String  presentacion;
    private String  dosis;
    private String  viaAdministracion;
    private String  frecuencia;
    private String  duracion;
    private Integer cantidadTotal;
    private String  indicaciones;
}
