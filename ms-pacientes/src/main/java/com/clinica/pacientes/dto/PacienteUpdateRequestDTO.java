package com.clinica.pacientes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Campos actualizables del paciente. Solo se modifican los campos enviados (no nulos).")
public class PacienteUpdateRequestDTO {

    @Schema(description = "Dirección de domicilio", example = "Jr. Las Flores 456, Miraflores")
    private String direccion;

    @Schema(description = "Teléfono o email de contacto", example = "987654321")
    private String contacto;

    @Schema(description = "Nombres del paciente", example = "María Elena")
    private String nombres;

    @Schema(description = "Apellidos del paciente", example = "Torres Vásquez")
    private String apellidos;
}
