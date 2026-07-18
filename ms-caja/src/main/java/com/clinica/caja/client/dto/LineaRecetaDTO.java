package com.clinica.caja.client.dto;

import lombok.Data;

@Data
public class LineaRecetaDTO {
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
