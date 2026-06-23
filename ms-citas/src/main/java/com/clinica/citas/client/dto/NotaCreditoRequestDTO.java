package com.clinica.citas.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotaCreditoRequestDTO {
    private Long idCita;
    private String motivo;
}
