package com.clinica.caja.client.dto;

import lombok.Data;

@Data
public class PersonalDTO {
    private Long id;
    private String nombres;
    private String apellidos;
    private String tipoPersonal;
    private PersonalMedicoDTO medicoInfo; // presente solo si tipoPersonal = MEDICO
}
