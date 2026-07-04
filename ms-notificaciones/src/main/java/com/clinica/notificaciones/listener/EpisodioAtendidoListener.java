package com.clinica.notificaciones.listener;

import com.clinica.notificaciones.config.RabbitMQConfig;
import com.clinica.notificaciones.event.EpisodioAtendidoEvent;
import com.clinica.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EpisodioAtendidoListener {

    private final NotificacionService notificacionService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EPISODIO_ATENDIDO)
    public void onEpisodioAtendido(EpisodioAtendidoEvent evento) {
        log.info("Evento EpisodioAtendido recibido: cita={}", evento.getIdCita());
        try {
            notificacionService.notificarEpisodioAtendido(evento);
        } catch (Exception ex) {
            log.error("Error procesando EpisodioAtendido para cita={}: {}", evento.getIdCita(), ex.getMessage());
        }
    }
}
