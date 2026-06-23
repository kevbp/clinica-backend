package com.clinica.caja.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "items_proforma")
@Getter @Setter @NoArgsConstructor
public class ItemProforma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proforma", nullable = false)
    private Proforma proforma;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoItem tipo;

    @Column(name = "id_item", nullable = false)
    private Long idItem; // idMedicamento o idExamen

    // Solo para tipo EXAMEN: ObjectId MongoDB de ms-historias-clinicas, necesario para autorizar
    @Column(name = "id_episodio_clinico")
    private String idEpisodioClinico;

    @Column
    private String descripcion;

    @Column(name = "precio_congelado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCongelado;

    @Column
    private Integer cantidad; // solo MEDICAMENTO

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoItem estado;
}
