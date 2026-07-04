package com.clinica.personal.dto;

import com.clinica.personal.model.TipoPersonal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Campos actualizables del personal. Solo se modifican los campos no nulos.")
public class PersonalUpdateRequestDTO {

    @Schema(description = "Nombres del personal", example = "Carlos Alberto")
    private String nombres;

    @Schema(description = "Apellidos del personal", example = "Ramírez Soto")
    private String apellidos;

    @Pattern(regexp = "^[A-Za-z0-9]{8,12}$", message = "Documento de identidad inválido")
    @Schema(description = "Número de documento de identidad", example = "12345678")
    private String documentoIdentidad;

    @Pattern(regexp = "^\\d{9}$", message = "El celular debe tener 9 dígitos")
    @Schema(description = "Número de celular", example = "987654321")
    private String celular;

    @Email(message = "Formato de correo inválido")
    @Schema(description = "Correo electrónico", example = "carlos.perez@clinica.pe")
    private String correo;

    @Schema(description = "Fecha de ingreso a la clínica", example = "2023-03-15")
    private LocalDate fechaIngreso;

    @Schema(description = "Tipo de personal. Si se cambia a MEDICO, numeroColegiatura e idEspecialidad son obligatorios.")
    private TipoPersonal tipoPersonal;

    @Schema(description = "Número de colegiatura. Requerido cuando tipoPersonal cambia a MEDICO.", example = "CMP-12345")
    private String numeroColegiatura;

    @Schema(description = "ID de especialidad. Requerido cuando tipoPersonal cambia a MEDICO.", example = "2")
    private Long idEspecialidad;
}
