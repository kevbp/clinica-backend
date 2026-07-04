package com.clinica.notificaciones.listener;

import com.clinica.notificaciones.config.RabbitMQConfig;
import com.clinica.notificaciones.event.ReenviarNotaCreditoEvent;
import com.clinica.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReenviarNotaCreditoListener {

    private final NotificacionService notificacionService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NC_REENVIAR)
    public void onReenviarNotaCredito(ReenviarNotaCreditoEvent evento) {
        log.info("Recibido ReenviarNC: numero={} correo={}", evento.getNumero(), evento.getCorreoDestino());
        notificacionService.notificarReenvioNotaCredito(evento);
    }
}
