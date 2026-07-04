package com.clinica.caja.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamenAutorizadoClientDTO {
    private Long id;
    private Long idPaciente;
    private String idEpisodioClinico;
    private Long idExamen;
    private String nombreExamen;
    private LocalDateTime fechaAutorizacion;
}
