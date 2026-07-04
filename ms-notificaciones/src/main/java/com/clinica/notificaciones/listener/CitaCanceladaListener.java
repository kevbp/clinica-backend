package com.clinica.notificaciones.listener;

import com.clinica.notificaciones.config.RabbitMQConfig;
import com.clinica.notificaciones.event.CitaCanceladaEvent;
import com.clinica.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CitaCanceladaListener {

    private final NotificacionService notificacionService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CITA_CANCELADA)
    public void onCitaCancelada(CitaCanceladaEvent evento) {
        log.info("Evento CitaCancelada recibido: cita={}, paciente={}", evento.getIdCita(), evento.getIdPaciente());
        try {
            notificacionService.notificarCitaCancelada(evento);
        } catch (Exception ex) {
            log.error("Error procesando CitaCancelada para cita={}: {}", evento.getIdCita(), ex.getMessage());
        }
    }
}
