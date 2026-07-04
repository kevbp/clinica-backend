package com.clinica.personal.dto;

import com.clinica.personal.model.TipoPersonal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Datos para registrar un nuevo miembro del personal. "
        + "Cuando tipoPersonal = MEDICO, los campos numeroColegiatura e idEspecialidad son obligatorios.")
public class PersonalRequestDTO {

    @NotBlank
    @Schema(description = "Nombres del personal", example = "Carlos Alberto",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombres;

    @NotBlank
    @Schema(description = "Apellidos del personal", example = "Pérez Gómez",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String apellidos;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9]{8,12}$", message = "Documento de identidad inválido")
    @Schema(description = "Número de documento de identidad", example = "12345678",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String documentoIdentidad;

    @Pattern(regexp = "^\\d{9}$", message = "El celular debe tener 9 dígitos")
    @Schema(description = "Número de celular", example = "987654321")
    private String celular;

    @Email(message = "Formato de correo inválido")
    @Schema(description = "Correo electrónico", example = "carlos.perez@clinica.pe")
    private String correo;

    @NotNull
    @Schema(description = "Fecha de ingreso a la clínica", example = "2024-01-15",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate fechaIngreso;

    @NotNull
    @Schema(description = "Tipo de personal",
            example = "MEDICO", requiredMode = Schema.RequiredMode.REQUIRED)
    private TipoPersonal tipoPersonal;

    @NotBlank
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "keycloakUserId debe ser un UUID válido")
    @Schema(description = "ID de usuario en Keycloak (subject del JWT)",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String keycloakUserId;

    @Schema(description = "Número de colegiatura médica. Requerido cuando tipoPersonal = MEDICO.",
            example = "CMP-12345")
    private String numeroColegiatura;

    @Schema(description = "ID de la especialidad médica. Requerido cuando tipoPersonal = MEDICO.",
            example = "2")
    private Long idEspecialidad;
}
