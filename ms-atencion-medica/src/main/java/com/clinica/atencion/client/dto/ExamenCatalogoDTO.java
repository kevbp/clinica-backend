package com.clinica.atencion.client.dto;

import lombok.Data;

@Data
public class ExamenCatalogoDTO {
    private Long id;
    private String nombre;
    private String categoria;
    private String descripcion;
}
