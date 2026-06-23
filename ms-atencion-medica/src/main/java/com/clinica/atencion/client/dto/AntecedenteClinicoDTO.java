package com.clinica.atencion.client.dto;

import lombok.Data;

@Data
public class AntecedenteClinicoDTO {
    private Long id;
    private Long idPaciente;
    private String descripcion;
    private String tipo; // "ENFERMEDAD_CRONICA", "ALERGIA", "OTRO"
}
