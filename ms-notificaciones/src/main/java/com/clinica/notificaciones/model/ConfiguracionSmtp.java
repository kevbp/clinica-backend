package com.clinica.notificaciones.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Fila única (id fijo = 1L): configuración SMTP activa del sistema, editable desde el frontend.
@Entity
@Table(name = "configuracion_smtp")
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracionSmtp {

    @Id
    private Long id = 1L;

    private String host;

    private Integer puerto;

    private String username;

    // Cifrada en reposo (AES/GCM) — ver CryptoUtil. Nunca se expone en texto plano vía API.
    @jakarta.persistence.Column(name = "password_cifrada", length = 1000)
    private String passwordCifrada;

    private String remitente;

    private Boolean starttlsEnabled = true;
}
