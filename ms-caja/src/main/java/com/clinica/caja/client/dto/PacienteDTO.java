package com.clinica.caja.client.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PacienteDTO {
    private Long id;
    private String nombres;
    private String apellidos;
    private String correo;
}
