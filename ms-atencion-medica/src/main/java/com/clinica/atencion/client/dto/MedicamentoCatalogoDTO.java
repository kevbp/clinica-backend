package com.clinica.atencion.client.dto;

import lombok.Data;

@Data
public class MedicamentoCatalogoDTO {
    private Long   id;
    private String nombre;
    private String principioActivo;
    private String presentacion;
}
