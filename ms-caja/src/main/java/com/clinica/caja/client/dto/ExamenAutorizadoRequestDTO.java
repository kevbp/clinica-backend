package com.clinica.caja.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExamenAutorizadoRequestDTO {
    private Long idPaciente;
    private String idEpisodioClinico;
    private Long idExamen;
}
