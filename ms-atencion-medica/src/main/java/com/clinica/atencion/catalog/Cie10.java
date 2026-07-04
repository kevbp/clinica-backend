package com.clinica.atencion.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cie10")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cie10 {

    @Id
    @Column(length = 10)
    private String codigo;

    @Column(nullable = false, length = 512)
    private String descripcion;

    @Column(length = 128)
    private String categoria;
}
