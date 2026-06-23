package com.clinica.laboratorio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "examenes_autorizados")
@Getter
@Setter
@NoArgsConstructor
public class ExamenAutorizado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencia débil inter-dominio hacia ms-pacientes
    @Column(name = "id_paciente", nullable = false)
    private Long idPaciente;

    // Referencia débil inter-dominio hacia ms-historias-clinicas (MongoDB ObjectId)
    @Column(name = "id_episodio_clinico", nullable = false)
    private String idEpisodioClinico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_examen", nullable = false)
    private Examen examen;

    @Column(name = "fecha_autorizacion", nullable = false)
    private LocalDateTime fechaAutorizacion;
}
