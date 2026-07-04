package com.clinica.notificaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Datos para actualizar la configuración SMTP")
public class ConfiguracionSmtpRequestDTO {

    @NotBlank
    @Schema(example = "smtp.gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String host;

    @NotNull
    @Min(1) @Max(65535)
    @Schema(example = "587", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer puerto;

    @NotBlank
    @Schema(example = "clinica.notificaciones@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "Password de la cuenta SMTP. Omitir o dejar vacío para conservar el actual.",
            example = "app-password-de-16-caracteres")
    private String password;

    @NotBlank
    @Email
    @Schema(example = "clinica.notificaciones@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String remitente;

    @NotNull
    @Schema(example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean starttlsEnabled;
}
