package com.clinica.historias.event;

import com.clinica.historias.config.RabbitMQConfig;
import com.clinica.historias.event.dto.EpisodioFinalizadoEvent;
import com.clinica.historias.service.HistoriasClinicasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EpisodioFinalizadoListener {

    private final HistoriasClinicasService historiasClinicasService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EPISODIO)
    public void onEpisodioFinalizado(EpisodioFinalizadoEvent event) {
        log.info("Evento EpisodioFinalizado recibido para cita={}, paciente={}",
                event.getIdCita(), event.getIdPaciente());
        try {
            historiasClinicasService.procesarEpisodioFinalizado(event);
            log.info("EpisodioClinico consolidado correctamente para cita={}", event.getIdCita());
        } catch (Exception ex) {
            log.error("Error al procesar EpisodioFinalizado para cita={}: {}",
                    event.getIdCita(), ex.getMessage(), ex);
            // No re-lanzar: el mensaje no se reencola para evitar bucles en este alcance académico
        }
    }
}
