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

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @OneToMany(mappedBy = "proforma", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemProforma> items = new ArrayList<>();
}
