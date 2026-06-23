package com.clinica.personal.dto;

import com.clinica.personal.model.TipoPersonal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Datos para registrar un nuevo miembro del personal")
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
    @Schema(description = "Número de documento de identidad", example = "12345678",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String documentoIdentidad;

    @Schema(description = "Teléfono o email de contacto", example = "987654321")
    private String contacto;

    @NotNull
    @Schema(description = "Fecha de ingreso a la clínica", example = "2024-01-15",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate fechaIngreso;

    @NotNull
    @Schema(description = "Tipo de personal",
            example = "MEDICO", requiredMode = Schema.RequiredMode.REQUIRED)
    private TipoPersonal tipoPersonal;

    @Schema(description = "ID de usuario en Keycloak (subject del JWT)",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private String keycloakUserId;
}
