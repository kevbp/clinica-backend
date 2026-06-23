package com.clinica.caja.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PagoConsultaConfirmadoEvent {
    private Long idCita;
    private Long idPaciente;
    private BigDecimal monto;
    private String correoPaciente;
}
