package com.clinica.historias.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Snapshot inmutable del médico al momento de la atención
@Getter
@Setter
@NoArgsConstructor
public class MedicoSnapshot {
    private Long id;
    private String nombres;
    private String apellidos;
    private String numeroColegiatura;
    private String especialidad;
}
