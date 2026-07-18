package com.clinica.atencion.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LineaRecetaEventDTO {
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
