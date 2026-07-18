package com.clinica.notificaciones.listener;

import com.clinica.notificaciones.client.AuditoriaClient;
import com.clinica.notificaciones.config.RabbitMQConfig;
import com.clinica.notificaciones.dto.AccionAuditoriaDTO;
import com.clinica.notificaciones.event.ReenviarNotaCreditoEvent;
import com.clinica.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReenviarNotaCreditoListener {

    private final NotificacionService notificacionService;
    private final AuditoriaClient     auditoriaClient;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NC_REENVIAR)
    public void onReenviarNotaCredito(Message message, ReenviarNotaCreditoEvent evento) {
        String cid = message.getMessageProperties().getCorrelationId();
        if (cid == null || cid.isBlank()) cid = UUID.randomUUID().toString();
        MDC.put("correlationId", cid);

        log.info("ARCH_LOG PROCESSING queue={} idNc={} correlationId={}",
                RabbitMQConfig.QUEUE_NC_REENVIAR, evento.getIdNotaCredito(), cid);

        final String cidRef = cid;
        CompletableFuture.runAsync(() -> {
            try { auditoriaClient.registrar(AccionAuditoriaDTO.builder()
                    .modulo("NOTIFICACIONES").accion("MSG_RECIBIDO")
                    .entidadTipo("NotaCredito").entidadId(String.valueOf(evento.getIdNotaCredito()))
                    .resultado("EXITO").correlationId(cidRef).build(), null);
            } catch (Exception ignored) {}
        });

        try {
            String resultado = notificacionService.notificarReenvioNotaCredito(evento);
            String accion    = accionAudit(resultado);
            String audRes    = NotificacionService.FALLIDO.equals(resultado) ? "ERROR" :
                               NotificacionService.ENVIADO.equals(resultado) ? "EXITO" : "INFO";
            final String accionRef = accion; final String audResRef = audRes;
            CompletableFuture.runAsync(() -> {
                try { auditoriaClient.registrar(AccionAuditoriaDTO.builder()
                        .modulo("NOTIFICACIONES").accion(accionRef)
                        .entidadTipo("NotaCredito").entidadId(String.valueOf(evento.getIdNotaCredito()))
                        .resultado(audResRef).correlationId(cidRef).build(), null);
                } catch (Exception ignored) {}
            });
            log.info("ARCH_LOG {} queue={} idNc={} correlationId={}",
                    resultado, RabbitMQConfig.QUEUE_NC_REENVIAR, evento.getIdNotaCredito(), cid);
        } catch (Exception ex) {
            String errorMsg = ex.getMessage();
            CompletableFuture.runAsync(() -> {
                try { auditoriaClient.registrar(AccionAuditoriaDTO.builder()
                        .modulo("NOTIFICACIONES").accion("NOTIF_FALLIDA")
                        .entidadTipo("NotaCredito").entidadId(String.valueOf(evento.getIdNotaCredito()))
                        .resultado("ERROR").correlationId(cidRef)
                        .errorDetalle(errorMsg).build(), null);
                } catch (Exception ignored) {}
            });
            log.error("ARCH_LOG ERROR queue={} idNc={} correlationId={} error={}",
                    RabbitMQConfig.QUEUE_NC_REENVIAR, evento.getIdNotaCredito(), cid, ex.getMessage());
            throw ex;
        } finally {
            MDC.clear();
        }
    }

    private String accionAudit(String resultado) {
        return switch (resultado) {
            case NotificacionService.OMITIDO -> "NOTIF_OMITIDA";
            case NotificacionService.FALLIDO -> "NOTIF_FALLIDA";
            default                          -> "NOTIF_ENVIADA";
        };
    }
}
