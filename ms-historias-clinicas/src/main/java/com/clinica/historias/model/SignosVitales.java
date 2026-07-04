package com.clinica.historias.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignosVitales {
    private Double  peso;
    private Double  talla;
    private String  presionArterial;
    private Integer frecuenciaCardiaca;
    private Double  temperatura;
    private Integer saturacionOxigeno;
    private Integer frecuenciaRespiratoria;
    private Double  imc;
}
