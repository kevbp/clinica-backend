package com.clinica.auditoria.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "accion_usuario")
@Getter @Setter @NoArgsConstructor
public class AccionUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_type", nullable = false, length = 20)
    private String logType = "ACTION_LOG";

    @Column(name = "correlation_id", length = 36)
    private String correlationId;

    // Extraído del JWT por ms-auditoria — nullable si la llamada llega sin token (tests, internos)
    @Column(name = "keycloak_user_id", length = 36)
    private String keycloakUserId;

    @Column(name = "rol", length = 50)
    private String rol;

    /** Módulo del frontend: CITAS, ATENCION, PACIENTES, PERSONAL, CAJA, CONFIGURACION */
    @Column(name = "modulo", nullable = false, length = 50)
    private String modulo;

    /** Tipo de acción: AGENDAR_CITA, CANCELAR_CITA, INICIAR_ATENCION, FINALIZAR_CONSULTA, etc. */
    @Column(name = "accion", nullable = false, length = 100)
    private String accion;

    /** Tipo de entidad afectada: CitaMedica, Paciente, Personal, etc. */
    @Column(name = "entidad_tipo", length = 100)
    private String entidadTipo;

    /** ID de la entidad afectada (como string para cubrir Long y ObjectId de Mongo) */
    @Column(name = "entidad_id", length = 100)
    private String entidadId;

    /** Nombre del evento AMQP que se dispara como consecuencia (ej: CitaCreada). Vincula ACTION_LOG con ARCH_LOG async. */
    @Column(name = "dispara_evento", length = 100)
    private String disparaEvento;

    /** Resultado: EXITO o ERROR */
    @Column(name = "resultado", length = 10)
    private String resultado;

    /** Metadatos adicionales no-sensibles en formato JSON (IDs, flags, httpStatus en error). */
    @Column(name = "metadatos", columnDefinition = "TEXT")
    private String metadatos;

    /** Mensaje de error del sistema cuando resultado = ERROR. Texto plano, nunca JSON. */
    @Column(name = "error_detalle", columnDefinition = "TEXT")
    private String errorDetalle;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // --- campos de infraestructura (ARCH_LOG) — null en ACTION_LOG ---

    @Column(name = "duracion_ms")
    private Long duracionMs;

    /** IP del cliente, "RabbitMQ", "DLQ". Equivale al campo origen del ESB de clase. */
    @Column(name = "origen", length = 100)
    private String origen;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "http_path", length = 255)
    private String httpPath;

    @Column(name = "http_status")
    private Integer httpStatus;
}
