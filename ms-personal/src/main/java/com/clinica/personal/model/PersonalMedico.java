package com.clinica.personal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "personal_medico")
@Getter
@Setter
@NoArgsConstructor
public class PersonalMedico {

    @Id
    @Column(name = "id_personal")
    private Long idPersonal;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_personal")
    private Personal personal;

    @Column(name = "numero_colegiatura", nullable = false, unique = true)
    private String numeroColegiatura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_especialidad", nullable = false)
    private Especialidad especialidad;
}
