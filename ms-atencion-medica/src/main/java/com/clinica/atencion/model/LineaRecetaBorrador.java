package com.clinica.atencion.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LineaRecetaBorrador {
    private Long    idMedicamento;
    private String  dosis;              // "500 mg" | "1 tableta"
    private String  viaAdministracion;  // "Oral" | "Intravenosa" | etc.
    private String  frecuencia;         // "Cada 8 horas" | "Una vez al día" | etc.
    private String  duracion;           // "7 días" | "14 días" | etc.
    private Integer cantidadTotal;      // calculado o ingresado manualmente
    private String  indicaciones;       // instrucciones libres para el paciente
}
