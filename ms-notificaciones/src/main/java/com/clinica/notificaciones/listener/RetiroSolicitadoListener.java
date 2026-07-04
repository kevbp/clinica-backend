package com.clinica.notificaciones.listener;

import com.clinica.notificaciones.config.RabbitMQConfig;
import com.clinica.notificaciones.event.RetiroSolicitadoEvent;
import com.clinica.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetiroSolicitadoListener {

    private final NotificacionService notificacionService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_RETIRO_SOLICITADO)
    public void onRetiroSolicitado(RetiroSolicitadoEvent evento) {
        log.info("Recibido RetiroSolicitado: id={} correo={}", evento.getIdRetiro(), evento.getCorreoDestino());
        notificacionService.notificarRetiroSolicitado(evento);
    }
}
