package com.clinica.citas.client.dto;

import lombok.Data;

@Data
public class PacienteDTO {
    private Long id;
    private String nombres;
    private String apellidos;
    private String documentoIdentidad;
    private String direccion;
    private String celular;
    private String correo;
}
