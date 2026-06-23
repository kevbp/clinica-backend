package com.clinica.caja.client.dto;

import lombok.Data;

@Data
public class PersonalMedicoDTO {
    private Long idPersonal;
    private String numeroColegiatura;
    private EspecialidadDTO especialidad;
}
