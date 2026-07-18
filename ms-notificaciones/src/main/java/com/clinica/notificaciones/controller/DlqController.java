package com.clinica.notificaciones.controller;

import com.clinica.notificaciones.client.AuditoriaClient;
import com.clinica.notificaciones.config.RabbitMQConfig;
import com.clinica.notificaciones.dto.AccionAuditoriaDTO;
import com.clinica.notificaciones.model.NotificacionFallida;
import com.clinica.notificaciones.repository.NotificacionFallidaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Retry de mensajes en DLQ.
 * Lee de la tabla notificacion_fallida (persistida por DlqNotificacionesListener)
 * y re-publica en el exchange/routing-key original — ya no compite con el listener
 * por los mensajes de la cola RabbitMQ.
 */
@Slf4j
@RestController
@RequestMapping("/notificaciones/dlq")
@RequiredArgsConstructor
public class DlqController {

    private final NotificacionFallidaRepository repository;
    private final RabbitTemplate                rabbitTemplate;
    private final AuditoriaClient               auditoriaClient;

    @PostMapping("/reintentar")
    public ResponseEntity<Map<String, Object>> reintentar(
            @RequestParam(defaultValue = "1") int cantidad) {

        List<NotificacionFallida> pendientes = repository
                .findByStatusOrderByFechaFalloDesc("PENDIENTE");

        List<Map<String, Object>> reintentados = new ArrayList<>();
        int procesados = 0;

        for (NotificacionFallida fallida : pendientes) {
            if (procesados >= cantidad) break;
            if (fallida.getExchangeOrigen() == null || fallida.getRoutingKeyOrigen() == null) {
                log.warn("DLQ id={} sin exchange/routingKey — no se puede reintentar", fallida.getId());
                continue;
            }

            try {
                // Reconstruir el mensaje con el payload original
                MessageProperties props = new MessageProperties();
                props.setCorrelationId(fallida.getCorrelationId());
                props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                Message msg = new Message(
                        fallida.getPayload() != null ? fallida.getPayload().getBytes() : new byte[0],
                        props);

                rabbitTemplate.send(fallida.getExchangeOrigen(), fallida.getRoutingKeyOrigen(), msg);

                fallida.setStatus("RESUELTO");
                repository.save(fallida);

                log.info("DLQ_REINTENTO id={} correlationId={} exchange={} rk={}",
                        fallida.getId(), fallida.getCorrelationId(),
                        fallida.getExchangeOrigen(), fallida.getRoutingKeyOrigen());

                final String cidFinal  = fallida.getCorrelationId();
                final String colaFinal = fallida.getCola() != null ? fallida.getCola() : "desconocida";
                CompletableFuture.runAsync(() -> {
                    try {
                        auditoriaClient.registrar(AccionAuditoriaDTO.builder()
                                .modulo("NOTIFICACIONES")
                                .accion("DLQ_REINTENTO")
                                .entidadTipo("MensajeDLQ")
                                .resultado("EXITO")
                                .correlationId(cidFinal)
                                .origen("DLQ")
                                .metadatos("{\"cola\":\"" + colaFinal + "\",\"reintentado\":true}")
                                .build(), null);
                    } catch (Exception ignored) {}
                });

                reintentados.add(Map.of(
                    "id",            fallida.getId(),
                    "correlationId", fallida.getCorrelationId() != null ? fallida.getCorrelationId() : "UNKNOWN",
                    "cola",          colaFinal,
                    "estado",        "RE_ENCOLADO"
                ));
                procesados++;

            } catch (Exception ex) {
                log.error("Error al reintentar DLQ id={}: {}", fallida.getId(), ex.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of(
            "reintentados",  reintentados.size(),
            "detalle",       reintentados,
            "dlq",           RabbitMQConfig.DLQ_NOTIFICACIONES,
            "accion",        reintentados.isEmpty()
                             ? "No hay mensajes PENDIENTE en DLQ o sin exchange/routingKey"
                             : "Mensajes re-encolados al exchange original"
        ));
    }
}
