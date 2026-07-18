package com.clinica.historias.event;

import com.clinica.historias.client.AuditoriaClient;
import com.clinica.historias.config.RabbitMQConfig;
import com.clinica.historias.dto.AccionAuditoriaDTO;
import com.clinica.historias.event.dto.EpisodioFinalizadoEvent;
import com.clinica.historias.service.HistoriasClinicasService;
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
public class EpisodioFinalizadoListener {

    private final HistoriasClinicasService historiasClinicasService;
    private final AuditoriaClient auditoriaClient;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EPISODIO)
    public void onEpisodioFinalizado(Message message, EpisodioFinalizadoEvent event) {
        String cid = message.getMessageProperties().getCorrelationId();
        if (cid == null || cid.isBlank()) cid = UUID.randomUUID().toString();
        MDC.put("correlationId", cid);

        log.info("{\"logType\":\"ARCH_LOG\",\"service\":\"ms-historias-clinicas\","
                + "\"event\":\"PROCESSING\",\"queue\":\"{}\",\"idCita\":{},\"correlationId\":\"{}\"}",
                RabbitMQConfig.QUEUE_EPISODIO, event.getIdCita(), cid);

        String cidRef = cid;
        CompletableFuture.runAsync(() -> {
            try { auditoriaClient.registrar(AccionAuditoriaDTO.builder().modulo("HISTORIAS_CLINICAS").accion("MSG_RECIBIDO").entidadTipo("EpisodioFinalizado").entidadId(String.valueOf(event.getIdCita())).resultado("EXITO").correlationId(cidRef).build(), null); } catch (Exception ignored) {}
        });

        try {
            historiasClinicasService.procesarEpisodioFinalizado(event);
            CompletableFuture.runAsync(() -> {
                try { auditoriaClient.registrar(AccionAuditoriaDTO.builder().modulo("HISTORIAS_CLINICAS").accion("EPISODIO_REGISTRADO").entidadTipo("EpisodioClinico").entidadId(String.valueOf(event.getIdCita())).resultado("EXITO").correlationId(cidRef).metadatos("{\"idCita\":" + event.getIdCita() + "}").build(), null); } catch (Exception ignored) {}
            });
            log.info("{\"logType\":\"ARCH_LOG\",\"service\":\"ms-historias-clinicas\","
                    + "\"event\":\"COMPLETED\",\"idCita\":{},\"correlationId\":\"{}\"}",
                    event.getIdCita(), cid);
        } catch (Exception ex) {
            log.error("{\"logType\":\"ARCH_LOG\",\"service\":\"ms-historias-clinicas\","
                    + "\"event\":\"FAILED\",\"idCita\":{},\"correlationId\":\"{}\",\"error\":\"{}\"}",
                    event.getIdCita(), cid, ex.getMessage(), ex);
            String errorMsg = ex.getMessage();
            CompletableFuture.runAsync(() -> {
                try {
                    auditoriaClient.registrar(AccionAuditoriaDTO.builder()
                            .modulo("HISTORIAS_CLINICAS")
                            .accion("EPISODIO_FALLIDO")
                            .entidadTipo("EpisodioClinico")
                            .entidadId(String.valueOf(event.getIdCita()))
                            .resultado("ERROR")
                            .correlationId(cidRef)
                            .errorDetalle(errorMsg)
                            .build(), null);
                } catch (Exception ignored) { }
            });
            throw new RuntimeException("Fallo al procesar EpisodioFinalizado idCita=" + event.getIdCita(), ex);
        } finally {
            MDC.clear();
        }
    }
}
