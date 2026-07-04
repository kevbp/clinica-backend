package com.clinica.atencion.client.dto;

import lombok.Data;

@Data
public class PersonalDTO {
    private Long id;
    private String nombres;
    private String apellidos;
    private MedicoInfoDTO medicoInfo;

    @Data
    public static class MedicoInfoDTO {
        private String numeroColegiatura;
        private EspecialidadDTO especialidad;
    }

    @Data
    public static class EspecialidadDTO {
        private Long id;
        private String nombre;
    }
}
