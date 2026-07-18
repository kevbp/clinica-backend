package com.clinica.historias.event;

import com.clinica.historias.client.AuditoriaClient;
import com.clinica.historias.config.RabbitMQConfig;
import com.clinica.historias.dto.AccionAuditoriaDTO;
import com.clinica.historias.model.EpisodioFallido;
import com.clinica.historias.repository.EpisodioFallidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Consume historias.dlq y persiste episodios fallidos en MongoDB para trazabilidad.
 * No reintenta — el mensaje ya agotó sus 3 intentos en la cola principal.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DlqHistoriasListener {

    private final EpisodioFallidoRepository repository;
    private final AuditoriaClient auditoriaClient;

    @RabbitListener(queues = RabbitMQConfig.DLQ_HISTORIAS)
    public void onEpisodioFallido(Message message) {
        String cid = message.getMessageProperties().getCorrelationId();
        if (cid == null || cid.isBlank()) cid = UUID.randomUUID().toString();
        MDC.put("correlationId", cid);

        String payload = new String(message.getBody());
        Object xDeath = message.getMessageProperties().getHeaders().get("x-death");
        String errorMensaje = xDeath != null ? xDeath.toString() : "Sin detalle de error";

        log.error("{\"logType\":\"ARCH_LOG\",\"service\":\"ms-historias-clinicas\",\"event\":\"DLQ\","
                + "\"queue\":\"{}\",\"correlationId\":\"{}\"}", RabbitMQConfig.DLQ_HISTORIAS, cid);

        try {
            EpisodioFallido fallido = new EpisodioFallido();
            fallido.setCorrelationId(cid);
            fallido.setPayload(payload);
            fallido.setErrorMensaje(errorMensaje);
            fallido.setFechaFallo(LocalDateTime.now());
            fallido.setReintentos(3);
            repository.save(fallido);

            String cidSnap = cid;
            long tsSnap = System.currentTimeMillis();
            CompletableFuture.runAsync(() -> {
                try {
                    auditoriaClient.registrar(AccionAuditoriaDTO.builder()
                            .modulo("HISTORIAS_CLINICAS").accion("MSG_RECIBIDO_DLQ")
                            .entidadTipo("EpisodioFinalizado").resultado("ERROR")
                            .correlationId(cidSnap).disparaEvento(RabbitMQConfig.DLQ_HISTORIAS)
                            .timestamp(tsSnap).build(), null);
                } catch (Exception ignored) {}
            });
        } catch (Exception ex) {
            log.error("Error al persistir episodio en DLQ Mongo: {}", ex.getMessage());
        } finally {
            MDC.clear();
        }
    }
}
