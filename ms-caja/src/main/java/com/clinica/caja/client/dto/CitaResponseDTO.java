package com.clinica.caja.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CitaResponseDTO {
    private Long id;
    private Long idPaciente;
    private Long idPersonal;
    private Long idConsultorio;
    private LocalDateTime fechaHora;
    private String estado;
}
