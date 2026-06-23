package com.clinica.atencion.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CitaMedicaDTO {
    private Long id;
    private Long idPaciente;
    private Long idPersonal;
    private Long idConsultorio;
    private LocalDateTime fechaHora;
    private String estado; // String para evitar acoplamiento con enum de ms-citas
}
