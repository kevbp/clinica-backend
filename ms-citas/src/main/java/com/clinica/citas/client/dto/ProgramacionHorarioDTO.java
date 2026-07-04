package com.clinica.citas.client.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ProgramacionHorarioDTO {
    private Long id;
    private Long idPersonal;
    private ConsultorioDTO consultorio;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
}
