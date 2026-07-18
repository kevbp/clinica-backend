package com.clinica.notificaciones.listener;

import com.clinica.notificaciones.client.AuditoriaClient;
import com.clinica.notificaciones.config.RabbitMQConfig;
import com.clinica.notificaciones.dto.AccionAuditoriaDTO;
import com.clinica.notificaciones.event.CitaCreadaEvent;
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
public class CitaCreadaListener {

    private final NotificacionService notificacionService;
    private final AuditoriaClient     auditoriaClient;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CITA_CREADA)
    public void onCitaCreada(Message message, CitaCreadaEvent evento) {
        String cid = message.getMessageProperties().getCorrelationId();
        if (cid == null || cid.isBlank()) cid = UUID.randomUUID().toString();
        MDC.put("correlationId", cid);

        log.info("ARCH_LOG PROCESSING queue={} idCita={} correlationId={}",
                RabbitMQConfig.QUEUE_CITA_CREADA, evento.getIdCita(), cid);

        final String cidRef = cid;
        CompletableFuture.runAsync(() -> {
            try { auditoriaClient.registrar(AccionAuditoriaDTO.builder()
                    .modulo("NOTIFICACIONES").accion("MSG_RECIBIDO")
                    .entidadTipo("CitaCreada").entidadId(String.valueOf(evento.getIdCita()))
                    .resultado("EXITO").correlationId(cidRef).build(), null);
            } catch (Exception ignored) {}
        });

        try {
            String resultado = notificacionService.notificarCitaCreada(evento);
            String accion    = accionAudit(resultado);
            String audRes    = NotificacionService.ENVIADO.equals(resultado) ? "EXITO" : "INFO";
            if (NotificacionService.FALLIDO.equals(resultado)) audRes = "ERROR";

            final String accionRef = accion;
            final String audResRef = audRes;
            CompletableFuture.runAsync(() -> {
                try { auditoriaClient.registrar(AccionAuditoriaDTO.builder()
                        .modulo("NOTIFICACIONES").accion(accionRef)
                        .entidadTipo("CitaCreada").entidadId(String.valueOf(evento.getIdCita()))
                        .resultado(audResRef).correlationId(cidRef).build(), null);
                } catch (Exception ignored) {}
            });
            log.info("ARCH_LOG {} queue={} idCita={} correlationId={}",
                    resultado, RabbitMQConfig.QUEUE_CITA_CREADA, evento.getIdCita(), cid);

        } catch (Exception ex) {
            String errorMsg = ex.getMessage();
            CompletableFuture.runAsync(() -> {
                try { auditoriaClient.registrar(AccionAuditoriaDTO.builder()
                        .modulo("NOTIFICACIONES").accion("NOTIF_FALLIDA")
                        .entidadTipo("CitaCreada").entidadId(String.valueOf(evento.getIdCita()))
                        .resultado("ERROR").correlationId(cidRef)
                        .errorDetalle(errorMsg).build(), null);
                } catch (Exception ignored) {}
            });
            log.error("ARCH_LOG ERROR queue={} idCita={} correlationId={} error={}",
                    RabbitMQConfig.QUEUE_CITA_CREADA, evento.getIdCita(), cid, ex.getMessage());
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
