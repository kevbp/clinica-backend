package com.clinica.caja.client.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrdenDTO {
    private String idOrden;
    private String idEpisodioClinico;
    private Long idPaciente;
    private Long idPersonalMedico;
    private List<LineaOrdenDTO> lineas;
}
