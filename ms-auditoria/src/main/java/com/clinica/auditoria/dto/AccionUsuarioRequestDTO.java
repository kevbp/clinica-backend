package com.clinica.auditoria.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload enviado por cada microservicio de dominio al registrar una acción.
 * keycloakUserId y rol NO se incluyen aquí — ms-auditoria los extrae del JWT
 * que viaja en el header Authorization de la misma llamada.
 */
@Getter @Setter @NoArgsConstructor
public class AccionUsuarioRequestDTO {

    @NotBlank
    private String modulo;

    @NotBlank
    private String accion;

    private String entidadTipo;
    private String entidadId;

    /** Nombre del evento AMQP disparado (ej: "CitaCreada"). Vincula ACTION_LOG con el flujo async. */
    private String disparaEvento;

    private String resultado; // EXITO | ERROR (default EXITO si null)

    private String correlationId;

    /** JSON con datos no-sensibles: IDs, flags, httpStatus. Nunca datos clínicos ni personales. */
    private String metadatos;

    /** Mensaje de error del sistema cuando resultado = ERROR. Texto plano. */
    private String errorDetalle;

    /**
     * Epoch millis del momento en que el evento ocurrió en el emisor.
     * Si se omite, ms-auditoria usa su propio reloj como fallback.
     */
    private Long    timestamp;

    /** "ACTION_LOG" (default) | "ARCH_LOG". Los filtros HTTP setean "ARCH_LOG". */
    private String  logType;

    // Campos de infraestructura — solo ARCH_LOG los popula
    private Long    duracionMs;
    private String  origen;
    private String  httpMethod;
    private String  httpPath;
    private Integer httpStatus;
}
