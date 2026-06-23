package com.clinica.pacientes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Datos para registrar un nuevo paciente")
public class PacienteRequestDTO {

    @NotBlank
    @Schema(description = "Número de documento de identidad", example = "87654321",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String documentoIdentidad;

    @NotBlank
    @Schema(description = "Nombres del paciente", example = "María Elena",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombres;

    @NotBlank
    @Schema(description = "Apellidos del paciente", example = "Torres Vásquez",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String apellidos;

    @Schema(description = "Dirección de domicilio", example = "Av. Los Pinos 123, Lima")
    private String direccion;

    @Schema(description = "Teléfono o email de contacto", example = "912345678")
    private String contacto;
}
