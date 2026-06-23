package com.clinica.personal.dto;

import com.clinica.personal.model.TipoPersonal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Perfil completo de un miembro del personal")
public class PersonalResponseDTO {

    @Schema(description = "ID interno", example = "5")
    private Long id;

    @Schema(description = "Nombres", example = "Carlos Alberto")
    private String nombres;

    @Schema(description = "Apellidos", example = "Pérez Gómez")
    private String apellidos;

    @Schema(description = "Número de documento de identidad", example = "12345678")
    private String documentoIdentidad;

    @Schema(description = "Teléfono o email de contacto", example = "987654321")
    private String contacto;

    @Schema(description = "Fecha de ingreso", example = "2024-01-15")
    private LocalDate fechaIngreso;

    @Schema(description = "Indica si el personal está activo y habilitado", example = "true")
    private Boolean estadoActivo;

    @Schema(description = "Tipo de personal", example = "MEDICO")
    private TipoPersonal tipoPersonal;

    @Schema(description = "ID de usuario en Keycloak", example = "550e8400-e29b-41d4-a716-446655440000")
    private String keycloakUserId;

    @Schema(description = "Extensión médica (presente solo si tipoPersonal = MEDICO)")
    private PersonalMedicoResponseDTO medicoInfo;
}
