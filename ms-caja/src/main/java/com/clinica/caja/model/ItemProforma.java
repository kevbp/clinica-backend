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

    /** Solo para tipo EXAMEN: ObjectId MongoDB de ms-historias-clinicas, necesario para autorizar. */
    @Column(name = "id_episodio_clinico")
    private String idEpisodioClinico;

    /** Nombre del medicamento o examen (dato duro). */
    @Column(name = "nombre_item")
    private String nombreItem;

    /** Principio activo (solo MEDICAMENTO, dato duro). */
    @Column(name = "principio_activo")
    private String principioActivo;

    /** Presentación del medicamento (solo MEDICAMENTO, dato duro). */
    @Column(name = "presentacion")
    private String presentacion;

    /** Dosis prescrita (solo MEDICAMENTO, dato duro). */
    @Column(name = "dosis")
    private String dosis;

    /** Frecuencia de administración (solo MEDICAMENTO, dato duro). */
    @Column(name = "frecuencia")
    private String frecuencia;

    /** Duración del tratamiento (solo MEDICAMENTO, dato duro). */
    @Column(name = "duracion")
    private String duracion;

    /** Indicaciones de preparación del examen (solo EXAMEN, dato duro). */
    @Column(name = "indicaciones_preparacion", columnDefinition = "TEXT")
    private String indicacionesPreparacion;

    /** Categoría del examen (solo EXAMEN, dato duro). */
    @Column(name = "categoria")
    private String categoria;

    /** Precio unitario congelado al momento de construir la proforma. */
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    /** Precio total congelado (precioUnitario × cantidad para MEDICAMENTO; = precioUnitario para EXAMEN). */
    @Column(name = "precio_congelado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCongelado;

    /** Cantidad a descontar de inventario (solo MEDICAMENTO). */
    @Column
    private Integer cantidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoItem estado;
}
