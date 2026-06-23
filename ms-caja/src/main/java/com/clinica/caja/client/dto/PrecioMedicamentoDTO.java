package com.clinica.caja.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrecioMedicamentoDTO {
    private Long idMedicamento;
    private BigDecimal precio;
}
