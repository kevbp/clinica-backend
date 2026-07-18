package com.clinica.farmacia.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LoteUpdateRequestDTO {
    private String numeroLote;
    private LocalDate fechaVencimiento;
    private Integer cantidadDisponible;
}
