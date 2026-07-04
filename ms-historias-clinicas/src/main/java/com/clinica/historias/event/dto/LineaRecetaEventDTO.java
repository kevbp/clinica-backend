package com.clinica.historias.event.dto;

import lombok.Data;

@Data
public class LineaRecetaEventDTO {
    private Long    idMedicamento;
    private String  dosis;
    private String  viaAdministracion;
    private String  frecuencia;
    private String  duracion;
    private Integer cantidadTotal;
    private String  indicaciones;
}
