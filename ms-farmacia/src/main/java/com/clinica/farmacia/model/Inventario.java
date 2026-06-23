package com.clinica.farmacia.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventario")
@Getter
@Setter
@NoArgsConstructor
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lote", nullable = false, unique = true)
    private Lote lote;

    @Column(name = "cantidad_disponible", nullable = false)
    private Integer cantidadDisponible;
}
