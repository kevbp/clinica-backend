package com.clinica.notificaciones.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccionAuditoriaDTO {
    private String modulo;
    private String accion;
    private String entidadTipo;
    private String entidadId;
    private String resultado;
    private String correlationId;
    private String disparaEvento;
    private String metadatos;
    private String errorDetalle;

    /** Epoch millis del momento en que el evento ocurrió en el emisor. Fijado automáticamente al construir. */
    @Builder.Default
    private Long    timestamp = System.currentTimeMillis();

    // Campos de infraestructura — solo ARCH_LOG los popula
    private String  logType;
    private Long    duracionMs;
    private String  origen;
    private String  httpMethod;
    private String  httpPath;
    private Integer httpStatus;
}
