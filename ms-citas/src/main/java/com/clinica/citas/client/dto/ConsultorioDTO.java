package com.clinica.citas.client.dto;

import lombok.Data;

@Data
public class ConsultorioDTO {
    private Long id;
    private String numero;
    private Integer piso;
    private String ubicacion;
}
