package com.clinica.pacientes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import com.clinica.pacientes.model.GrupoSanguineo;
import com.clinica.pacientes.model.Sexo;

import java.time.LocalDate;

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

    @Schema(description = "Fecha de nacimiento (null para pacientes registrados antes de este campo)", example = "1990-05-20")
    private LocalDate fechaNacimiento;

    @Schema(description = "Dirección de domicilio", example = "Av. Los Pinos 123, Lima")
    private String direccion;

    @Schema(description = "Número de celular", example = "987654321")
    private String celular;

    @Schema(description = "Correo electrónico", example = "maria.torres@correo.com")
    private String correo;

    @Schema(description = "Estado activo del paciente", example = "true")
    private Boolean estadoActivo;

    @Schema(description = "Sexo biológico", example = "FEMENINO")
    private Sexo sexo;

    @Schema(description = "Grupo sanguíneo", example = "O_POS")
    private GrupoSanguineo grupoSanguineo;

    @Schema(description = "Nombre del banco para retiros de saldo", example = "BCP")
    private String nombreBanco;

    @Schema(description = "Número de cuenta bancaria para retiros de saldo", example = "191-12345678-0-62")
    private String numeroCuenta;
}
