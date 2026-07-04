package com.clinica.horarios.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "programacion_horarios")
@Getter
@Setter
@NoArgsConstructor
public class ProgramacionHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencia débil inter-dominio: solo el identificador, sin FK real hacia ms-personal
    @Column(name = "id_personal", nullable = false)
    private Long idPersonal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_consultorio", nullable = false)
    private Consultorio consultorio;

    // Fecha concreta del turno (no recurrente). Habilita historial, edición puntual
    // y navegación por calendario; ms-citas derivará disponibilidad por fecha real.
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;
}
