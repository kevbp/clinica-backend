package com.clinica.pacientes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Perfil demográfico de un paciente")
public class PacienteResponseDTO {

    @Schema(description = "ID interno del paciente", example = "42")
    private Long id;

    @Schema(description = "Número de documento de identidad", example = "87654321")
    private String documentoIdentidad;

    @Schema(description = "Nombres", example = "María Elena")
    private String nombres;

    @Schema(description = "Apellidos", example = "Torres Vásquez")
    private String apellidos;

    @Schema(description = "Dirección de domicilio", example = "Av. Los Pinos 123, Lima")
    private String direccion;

    @Schema(description = "Teléfono o email de contacto", example = "912345678")
    private String contacto;
}
