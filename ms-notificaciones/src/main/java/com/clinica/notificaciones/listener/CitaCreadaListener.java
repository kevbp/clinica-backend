package com.clinica.notificaciones.listener;

import com.clinica.notificaciones.config.RabbitMQConfig;
import com.clinica.notificaciones.event.CitaCreadaEvent;
import com.clinica.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CitaCreadaListener {

    private final NotificacionService notificacionService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CITA_CREADA)
    public void onCitaCreada(CitaCreadaEvent evento) {
        log.info("Evento CitaCreada recibido: cita={}, paciente={}", evento.getIdCita(), evento.getIdPaciente());
        try {
            notificacionService.notificarCitaCreada(evento);
        } catch (Exception ex) {
            log.error("Error procesando CitaCreada para cita={}: {}", evento.getIdCita(), ex.getMessage());
        }
    }
}
