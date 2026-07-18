package com.clinica.farmacia.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_inventario")
@Getter @Setter @NoArgsConstructor
public class MovimientoInventario {

    public enum Tipo { ENTRADA, SALIDA, AJUSTE }
    public enum Motivo { LOTE_REGISTRADO, PAGO_PROFORMA, AJUSTE_MANUAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_medicamento", nullable = false)
    private Long idMedicamento;

    @Column(name = "nombre_medicamento", nullable = false)
    private String nombreMedicamento;

    @Column(name = "id_lote", nullable = false)
    private Long idLote;

    @Column(name = "numero_lote", nullable = false)
    private String numeroLote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Motivo motivo;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "saldo_anterior", nullable = false)
    private Integer saldoAnterior;

    @Column(name = "saldo_posterior", nullable = false)
    private Integer saldoPosterior;

    @Column
    private String referencia;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    public void prePersist() {
        if (fecha == null) fecha = LocalDateTime.now();
    }
}
