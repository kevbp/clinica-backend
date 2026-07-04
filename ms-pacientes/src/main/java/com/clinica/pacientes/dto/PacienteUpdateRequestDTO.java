package com.clinica.pacientes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import com.clinica.pacientes.model.GrupoSanguineo;
import com.clinica.pacientes.model.Sexo;

import java.time.LocalDate;

@Data
@Schema(description = "Campos actualizables del paciente. Solo se modifican los campos enviados (no nulos).")
public class PacienteUpdateRequestDTO {

    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    @Schema(description = "Fecha de nacimiento", example = "1990-05-20")
    private LocalDate fechaNacimiento;

    @Schema(description = "Dirección de domicilio", example = "Jr. Las Flores 456, Miraflores")
    private String direccion;

    @Pattern(regexp = "^\\d{9}$", message = "El celular debe tener 9 dígitos")
    @Schema(description = "Número de celular", example = "987654321")
    private String celular;

    @Email(message = "Formato de correo inválido")
    @Schema(description = "Correo electrónico", example = "maria.torres@correo.com")
    private String correo;

    @Schema(description = "Nombres del paciente", example = "María Elena")
    private String nombres;

    @Schema(description = "Apellidos del paciente", example = "Torres Vásquez")
    private String apellidos;

    @Schema(description = "Sexo biológico del paciente", example = "FEMENINO")
    private Sexo sexo;

    @Schema(description = "Grupo sanguíneo", example = "O_POS")
    private GrupoSanguineo grupoSanguineo;

    @Schema(description = "Nombre del banco para retiros de saldo", example = "BCP")
    private String nombreBanco;

    @Schema(description = "Número de cuenta bancaria para retiros de saldo", example = "191-12345678-0-62")
    private String numeroCuenta;
}
