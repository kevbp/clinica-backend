package com.clinica.notificaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Solicitud de envío de un correo de prueba con la configuración SMTP guardada")
public class ProbarConexionRequestDTO {

    @NotBlank
    @Email
    @Schema(description = "Dirección a la que se enviará el correo de prueba", example = "admin@clinica.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String correoDestino;
}
