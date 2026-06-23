package com.clinica.caja.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrecioExamenDTO {
    private Long idExamen;
    private BigDecimal precio;
}
