package com.clinica.auditoria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
public class AccionUsuarioResponseDTO {
    private Long id;
    private String logType;
    private String correlationId;
    private String keycloakUserId;
    private String rol;
    private String modulo;
    private String accion;
    private String entidadTipo;
    private String entidadId;
    private String disparaEvento;
    private String resultado;
    private String metadatos;
    private String errorDetalle;
    private LocalDateTime timestamp;

    // Campos de infraestructura (ARCH_LOG)
    private Long    duracionMs;
    private String  origen;
    private String  httpMethod;
    private String  httpPath;
    private Integer httpStatus;
}
