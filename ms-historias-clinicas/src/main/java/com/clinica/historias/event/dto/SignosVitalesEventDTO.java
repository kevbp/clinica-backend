package com.clinica.historias.event.dto;

import lombok.Data;

@Data
public class SignosVitalesEventDTO {
    private Double  peso;
    private Double  talla;
    private String  presionArterial;
    private Integer frecuenciaCardiaca;
    private Double  temperatura;
    private Integer saturacionOxigeno;
    private Integer frecuenciaRespiratoria;
    private Double  imc;
}
