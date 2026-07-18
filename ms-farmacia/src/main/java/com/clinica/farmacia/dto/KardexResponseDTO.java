package com.clinica.farmacia.dto;

import com.clinica.farmacia.model.MovimientoInventario;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Movimiento del kardex de inventario")
public class KardexResponseDTO {

    private Long id;
    private Long idMedicamento;
    private String nombreMedicamento;
    private Long idLote;
    private String numeroLote;
    private MovimientoInventario.Tipo tipo;
    private MovimientoInventario.Motivo motivo;
    private Integer cantidad;
    private Integer saldoAnterior;
    private Integer saldoPosterior;
    private String referencia;
    private LocalDateTime fecha;
}
