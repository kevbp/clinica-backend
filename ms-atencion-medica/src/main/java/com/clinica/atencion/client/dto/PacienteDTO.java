package com.clinica.atencion.client.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PacienteDTO {
    private Long id;
    private String nombres;
    private String apellidos;
    private String documentoIdentidad;
    private LocalDate fechaNacimiento;
    private String correo;
    private String sexo;
    private String grupoSanguineo;
}
