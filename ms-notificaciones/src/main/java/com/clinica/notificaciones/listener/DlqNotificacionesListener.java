package com.clinica.notificaciones.listener;

import com.clinica.notificaciones.client.AuditoriaClient;
import com.clinica.notificaciones.config.RabbitMQConfig;
import com.clinica.notificaciones.dto.AccionAuditoriaDTO;
import com.clinica.notificaciones.model.NotificacionFallida;
import com.clinica.notificaciones.repository.NotificacionFallidaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Único consumidor de la DLQ de notificaciones.
 * Fusiona DlqAuditoriaListener (audit en ms-auditoria) y la persistencia en notificacion_fallida.
 * Ambas acciones ocurren siempre para el mismo mensaje.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DlqNotificacionesListener {

    private final NotificacionFallidaRepository repository;
    private final AuditoriaClient               auditoriaClient;

    @RabbitListener(queues = RabbitMQConfig.DLQ_NOTIFICACIONES)
    public void onMensajeFallido(Message message) {
        String cid = message.getMessageProperties().getCorrelationId();
        if (cid == null || cid.isBlank()) cid = UUID.randomUUID().toString();
        MDC.put("correlationId", cid);
        MDC.put("logType", "ARCH_LOG");

        String cola       = message.getMessageProperties().getConsumerQueue();
        String exchange   = extraerDeath(message, "exchange", "");
        String routingKey = extraerRoutingKey(message);
        String deathCount = extraerDeathCount(message);
        String payload    = new String(message.getBody());

        log.error("ARCH_LOG DLQ_RCVD queue={} correlationId={} deathCount={}", cola, cid, deathCount);

        // 1. Persistir en tabla local para que el retry pueda releer exchange/routingKey
        try {
            String ahora = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String historial = String.format(
                "[{\"estado\":\"QUEUED\",\"ts\":\"n/a\"},{\"estado\":\"PROCESSING\",\"ts\":\"n/a\",\"intentos\":%s},{\"estado\":\"DLQ\",\"ts\":\"%s\"}]",
                deathCount, ahora);

            NotificacionFallida fallida = new NotificacionFallida();
            fallida.setCola(cola != null ? cola : RabbitMQConfig.DLQ_NOTIFICACIONES);
            fallida.setCorrelationId(cid);
            fallida.setExchangeOrigen(exchange);
            fallida.setRoutingKeyOrigen(routingKey);
            fallida.setPayload(payload);
            fallida.setErrorMensaje("x-death count=" + deathCount);
            fallida.setFechaFallo(LocalDateTime.now());
            fallida.setReintentos(Integer.parseInt(deathCount));
            fallida.setHistorialEstados(historial);
            fallida.setStatus("PENDIENTE");
            repository.save(fallida);
        } catch (Exception ex) {
            log.error("Error al persistir NotificacionFallida: {}", ex.getMessage());
        }

        // 2. Registrar MSG_EN_DLQ en ms-auditoria
        final String cidFinal   = cid;
        final String colaFinal  = cola  != null ? cola.replace("\"", "'")  : "unknown";
        final String countFinal = deathCount;
        CompletableFuture.runAsync(() -> {
            try {
                auditoriaClient.registrar(AccionAuditoriaDTO.builder()
                        .modulo("NOTIFICACIONES")
                        .accion("MSG_EN_DLQ")
                        .entidadTipo("MensajeMuerto")
                        .resultado("ERROR")
                        .correlationId(cidFinal)
                        .origen("DLQ")
                        .metadatos("{\"cola\":\"" + colaFinal + "\",\"deathCount\":" + countFinal + "}")
                        .build(), null);
            } catch (Exception ignored) {}
        });

        MDC.clear();
    }

    private String extraerDeath(Message msg, String key, String defaultVal) {
        try {
            List<Map<String, ?>> deaths = msg.getMessageProperties().getXDeathHeader();
            if (deaths != null && !deaths.isEmpty()) {
                Object val = deaths.get(0).get(key);
                return val != null ? val.toString() : defaultVal;
            }
        } catch (Exception ignored) {}
        return defaultVal;
    }

    private String extraerRoutingKey(Message msg) {
        try {
            List<Map<String, ?>> deaths = msg.getMessageProperties().getXDeathHeader();
            if (deaths != null && !deaths.isEmpty()) {
                Object rks = deaths.get(0).get("routing-keys");
                if (rks instanceof List<?> list && !list.isEmpty()) return list.get(0).toString();
            }
        } catch (Exception ignored) {}
        return "";
    }

    private String extraerDeathCount(Message msg) {
        try {
            List<Map<String, ?>> deaths = msg.getMessageProperties().getXDeathHeader();
            if (deaths != null && !deaths.isEmpty()) {
                Object count = deaths.get(0).get("count");
                return count != null ? count.toString() : "1";
            }
        } catch (Exception ignored) {}
        return "1";
    }
}
