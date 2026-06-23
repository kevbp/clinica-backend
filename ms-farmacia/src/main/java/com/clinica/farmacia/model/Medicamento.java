package com.clinica.farmacia.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "medicamentos")
@Getter
@Setter
@NoArgsConstructor
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "principio_activo", nullable = false)
    private String principioActivo;

    @Column(nullable = false)
    private String presentacion;

    // Precio almacenado pero NO expuesto en el DTO de catálogo.
    // Solo accesible vía GET /medicamentos/{id}/precio, exclusivo para ms-caja.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
}
