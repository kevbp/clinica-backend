package com.clinica.notificaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Configuración SMTP activa (el password nunca se expone en texto plano)")
public class ConfiguracionSmtpResponseDTO {

    @Schema(example = "smtp.gmail.com")
    private String host;

    @Schema(example = "587")
    private Integer puerto;

    @Schema(example = "clinica.notificaciones@gmail.com")
    private String username;

    @Schema(description = "true si ya hay un password guardado en BD", example = "true")
    private boolean passwordConfigurado;

    @Schema(example = "clinica.notificaciones@gmail.com")
    private String remitente;

    @Schema(example = "true")
    private boolean starttlsEnabled;
}
