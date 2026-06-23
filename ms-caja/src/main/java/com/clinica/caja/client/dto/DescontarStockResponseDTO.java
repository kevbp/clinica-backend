package com.clinica.caja.client.dto;

import lombok.Data;

@Data
public class DescontarStockResponseDTO {
    private boolean exitoso;
    private Integer cantidadSolicitada;
    private Integer cantidadDescontada;
    private String mensaje;
}
