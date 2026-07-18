package com.clinica.notificaciones.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion_fallida")
@Getter @Setter @NoArgsConstructor
public class NotificacionFallida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cola", nullable = false, length = 100)
    private String cola;

    @Column(name = "correlation_id", length = 36)
    private String correlationId;

    /** Acción de negocio que originó el mensaje (ej: AGENDAR_CITA). Opcional — depende de que el publicador lo embeba. */
    @Column(name = "accion_origen", length = 100)
    private String accionOrigen;

    /** ID de la entidad afectada (idCita, idPaciente, etc.) */
    @Column(name = "entidad_id", length = 100)
    private String entidadId;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "error_mensaje", columnDefinition = "TEXT")
    private String errorMensaje;

    @Column(name = "fecha_fallo", nullable = false)
    private LocalDateTime fechaFallo;

    @Column(name = "reintentos")
    private Integer reintentos;

    /**
     * Historial de transiciones de estado en formato JSON.
     * Ejemplo: [{"estado":"QUEUED","ts":"2026-07-10T19:00:00"},{"estado":"PROCESSING","ts":"..."},{"estado":"DLQ","ts":"..."}]
     */
    @Column(name = "historial_estados", columnDefinition = "TEXT")
    private String historialEstados;

    /** Exchange original extraído de x-death — necesario para el retry */
    @Column(name = "exchange_origen", length = 200)
    private String exchangeOrigen;

    /** Routing key original extraída de x-death — necesaria para el retry */
    @Column(name = "routing_key_origen", length = 200)
    private String routingKeyOrigen;

    /** Estado de resolución del fallo: PENDIENTE | RESUELTO | DESCARTADO */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDIENTE";
}
