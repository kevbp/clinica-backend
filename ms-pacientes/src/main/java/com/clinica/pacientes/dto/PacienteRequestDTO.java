package com.clinica.pacientes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import com.clinica.pacientes.model.GrupoSanguineo;
import com.clinica.pacientes.model.Sexo;

import java.time.LocalDate;

@Data
@Schema(description = "Datos para registrar un nuevo paciente")
public class PacienteRequestDTO {

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9]{8,12}$", message = "Documento de identidad inválido")
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

    @Schema(description = "Sexo biológico del paciente", example = "FEMENINO")
    private Sexo sexo;

    @Schema(description = "Grupo sanguíneo", example = "O_POS")
    private GrupoSanguineo grupoSanguineo;

    @NotNull
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    @Schema(description = "Fecha de nacimiento", example = "1990-05-20",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate fechaNacimiento;

    @Schema(description = "Dirección de domicilio", example = "Av. Los Pinos 123, Lima")
    private String direccion;

    @Pattern(regexp = "^\\d{9}$", message = "El celular debe tener 9 dígitos")
    @Schema(description = "Número de celular", example = "987654321")
    private String celular;

    @Email(message = "Formato de correo inválido")
    @Schema(description = "Correo electrónico", example = "maria.torres@correo.com")
    private String correo;
}
