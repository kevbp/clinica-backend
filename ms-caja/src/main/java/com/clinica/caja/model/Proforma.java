package com.clinica.caja.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proformas")
@Getter @Setter @NoArgsConstructor
public class Proforma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_paciente", nullable = false)
    private Long idPaciente;

    @Column(name = "id_receta")
    private String idReceta;

    @Column(name = "id_orden")
    private String idOrden;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoProforma tipo;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_vigencia", nullable = false)
    private LocalDateTime fechaVigencia;

    @OneToMany(mappedBy = "proforma", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemProforma> items = new ArrayList<>();
}
