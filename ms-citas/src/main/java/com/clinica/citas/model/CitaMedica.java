package com.clinica.citas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "citas_medicas")
@Getter
@Setter
@NoArgsConstructor
public class CitaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencias débiles inter-dominio (sin FK reales)
    @Column(name = "id_paciente", nullable = false)
    private Long idPaciente;

    @Column(name = "id_personal", nullable = false)
    private Long idPersonal;

    @Column(name = "id_consultorio", nullable = false)
    private Long idConsultorio;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado;
}
