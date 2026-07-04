package com.clinica.atencion.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignosVitalesBorrador {
    private Double  peso;                // kg
    private Double  talla;               // cm
    private String  presionArterial;     // "120/80"
    private Integer frecuenciaCardiaca;  // lpm
    private Double  temperatura;         // °C
    private Integer saturacionOxigeno;   // %
    private Integer frecuenciaRespiratoria; // rpm
    private Double  imc;                 // calculado: peso/(talla/100)^2
}
