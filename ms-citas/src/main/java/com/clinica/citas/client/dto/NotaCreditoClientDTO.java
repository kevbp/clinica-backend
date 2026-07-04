package com.clinica.citas.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Subconjunto de NotaCreditoResponseDTO de ms-caja para incrustar en CitaCanceladaEvent. */
@Data
@NoArgsConstructor
public class NotaCreditoClientDTO {
    private String numero;
    private BigDecimal monto;
    private BigDecimal montoRetenido;
    private String tipo;
}