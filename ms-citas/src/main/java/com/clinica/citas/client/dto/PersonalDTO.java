package com.clinica.citas.client.dto;

import lombok.Data;

@Data
public class PersonalDTO {
    private Long id;
    private String nombres;
    private String apellidos;
    private PersonalMedicoDTO medicoInfo;
}
