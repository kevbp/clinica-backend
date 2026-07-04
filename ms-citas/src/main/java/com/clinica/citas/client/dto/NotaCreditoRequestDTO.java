package com.clinica.citas.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotaCreditoRequestDTO {
    private Long idCita;
    private String motivo;
    /** Valor del enum TipoNotaCredito de ms-caja, enviado como String para evitar dependencia de tipo cruzado. */
    private String tipo;
}
