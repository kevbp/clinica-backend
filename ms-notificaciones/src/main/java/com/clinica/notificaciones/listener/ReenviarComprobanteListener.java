package com.clinica.notificaciones.listener;

import com.clinica.notificaciones.config.RabbitMQConfig;
import com.clinica.notificaciones.event.ReenviarComprobanteEvent;
import com.clinica.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReenviarComprobanteListener {

    private final NotificacionService notificacionService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_COMPROBANTE_REENVIAR)
    public void onReenviarComprobante(ReenviarComprobanteEvent evento) {
        log.info("Recibido ReenviarComprobante: numero={} correo={}", evento.getNumero(), evento.getCorreoDestino());
        notificacionService.notificarReenvioComprobante(evento);
    }
}
