package com.clinica.notificaciones.listener;

import com.clinica.notificaciones.config.RabbitMQConfig;
import com.clinica.notificaciones.event.CitaReagendadaEvent;
import com.clinica.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CitaReagendadaListener {

    private final NotificacionService notificacionService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CITA_REAGENDADA)
    public void onCitaReagendada(CitaReagendadaEvent evento) {
        log.info("Evento CitaReagendada recibido: cita={}, paciente={}", evento.getIdCita(), evento.getIdPaciente());
        try {
            notificacionService.notificarCitaReagendada(evento);
        } catch (Exception ex) {
            log.error("Error procesando CitaReagendada para cita={}: {}", evento.getIdCita(), ex.getMessage());
        }
    }
}
