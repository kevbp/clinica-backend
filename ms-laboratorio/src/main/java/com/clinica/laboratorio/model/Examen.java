package com.clinica.laboratorio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "examenes")
@Getter
@Setter
@NoArgsConstructor
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String categoria;

    private String descripcion;

    // Precio almacenado pero NO expuesto en el DTO de catálogo.
    // Solo accesible vía GET /examenes/{id}/precio, exclusivo para ms-caja.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
}
