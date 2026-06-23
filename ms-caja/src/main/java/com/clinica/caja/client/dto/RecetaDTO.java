package com.clinica.caja.client.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecetaDTO {
    private String idReceta;
    private String idEpisodioClinico;
    private Long idPaciente;
    private Long idPersonalMedico;
    private List<LineaRecetaDTO> lineas;
}
