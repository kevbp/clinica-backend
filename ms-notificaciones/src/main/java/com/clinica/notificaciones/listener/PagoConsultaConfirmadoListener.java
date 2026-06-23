package com.clinica.notificaciones.listener;

import com.clinica.notificaciones.config.RabbitMQConfig;
import com.clinica.notificaciones.event.PagoConsultaConfirmadoEvent;
import com.clinica.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagoConsultaConfirmadoListener {

    private final NotificacionService notificacionService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAGO_CONFIRMADO)
    public void onPagoConfirmado(PagoConsultaConfirmadoEvent evento) {
        log.info("Evento PagoConsultaConfirmado recibido: cita={}, paciente={}", evento.getIdCita(), evento.getIdPaciente());
        try {
            notificacionService.notificarPagoConfirmado(evento);
        } catch (Exception ex) {
            log.error("Error procesando PagoConsultaConfirmado para cita={}: {}", evento.getIdCita(), ex.getMessage());
        }
    }
}
