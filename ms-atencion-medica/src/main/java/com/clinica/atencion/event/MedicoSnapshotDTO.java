package com.clinica.atencion.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicoSnapshotDTO {
    private Long id;
    private String nombres;
    private String apellidos;
    private String numeroColegiatura;
    private String especialidad;
}
