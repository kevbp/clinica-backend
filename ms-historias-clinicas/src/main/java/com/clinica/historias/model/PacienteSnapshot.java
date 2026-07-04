package com.clinica.historias.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

// Snapshot inmutable del paciente al momento de la atención
@Getter
@Setter
@NoArgsConstructor
public class PacienteSnapshot {
    private Long id;
    private String nombres;
    private String apellidos;
    private String documentoIdentidad;
    private LocalDate fechaNacimiento;
}
