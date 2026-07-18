package com.clinica.caja.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DescontarStockRequestDTO {
    private Integer cantidad;
    private String referencia;

    public DescontarStockRequestDTO(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
