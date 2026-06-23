package com.clinica.caja.service;

import com.clinica.caja.client.CitasFeignClient;
import com.clinica.caja.client.PersonalFeignClient;
import com.clinica.caja.client.dto.EstadoCitaUpdateDTO;
import com.clinica.caja.client.dto.PersonalDTO;
import com.clinica.caja.config.RabbitMQConfig;
import com.clinica.caja.dto.PagoConsultaRequestDTO;
import com.clinica.caja.dto.PagoConsultaResponseDTO;
import com.clinica.caja.event.PagoConsultaConfirmadoEvent;
import com.clinica.caja.model.EstadoPagoConsulta;
import com.clinica.caja.model.PagoConsulta;
import com.clinica.caja.model.TarifaConsulta;
import com.clinica.caja.repository.PagoConsultaRepository;
import com.clinica.caja.repository.TarifaConsultaRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoConsultaService {

    private static final int MAX_REINTENTOS_CONFIRMAR = 3;

    private final PagoConsultaRepository    pagoRepository;
    private final TarifaConsultaRepository  tarifaRepository;
    private final PersonalFeignClient       personalClient;
    private final CitasFeignClient          citasClient;
    private final RabbitTemplate            rabbitTemplate;

    @Transactional
    public PagoConsultaResponseDTO crear(PagoConsultaRequestDTO request) {
        if (pagoRepository.findByIdCita(request.getIdCita()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un pago de consulta para la cita " + request.getIdCita());
        }

        PersonalDTO personal = personalClient.obtenerPersonal(request.getIdPersonalMedico()).getBody();
        if (personal == null || personal.getMedicoInfo() == null
                || personal.getMedicoInfo().getEspecialidad() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El personal " + request.getIdPersonalMedico() + " no tiene especialidad médica registrada.");
        }

        Long idEspecialidad = personal.getMedicoInfo().getEspecialidad().getId();
        TarifaConsulta tarifa = tarifaRepository.findById(idEspecialidad)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe tarifa de consulta para la especialidad " + idEspecialidad));

        PagoConsulta pago = new PagoConsulta();
        pago.setIdCita(request.getIdCita());
        pago.setIdPaciente(request.getIdPaciente());
        pago.setMonto(tarifa.getMonto());
        pago.setCorreoPaciente(request.getCorreoPaciente());
        pago.setEstado(EstadoPagoConsulta.PENDIENTE);

        return toResponse(pagoRepository.save(pago));
    }

    /**
     * Saga 14.1 — Confirmación de pago de consulta.
     * Orden: (1) marcar PAGADO local → (2) confirmar cita con reintentos
     *        → compensación si falla → (3) publicar evento si todo sale bien.
     */
    @Transactional
    public PagoConsultaResponseDTO confirmarPago(Long id) {
        PagoConsulta pago = pagoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "PagoConsulta no encontrado con id: " + id));

        if (pago.getEstado() != EstadoPagoConsulta.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede confirmar un pago en estado PENDIENTE.");
        }

        // Paso 3 (sagas.md §14.1): marcar PAGADO — ya irreversible en este punto
        pago.setEstado(EstadoPagoConsulta.PAGADO);
        pagoRepository.save(pago);

        // Paso 4: llamar síncronamente a ms-citas con reintento acotado
        boolean confirmada = intentarConfirmarCita(pago.getIdCita());

        if (!confirmada) {
            // Compensación: PAGADO_SIN_CONFIRMAR + cancelar slot
            log.warn("Saga 14.1 — compensación: pago id={} queda PAGADO_SIN_CONFIRMAR", id);
            pago.setEstado(EstadoPagoConsulta.PAGADO_SIN_CONFIRMAR);
            pagoRepository.save(pago);
            try {
                citasClient.compensarPagoFallido(pago.getIdCita());
                log.info("Saga 14.1 — slot liberado por compensarPagoFallido, cita={}", pago.getIdCita());
            } catch (Exception ex) {
                log.error("Saga 14.1 — compensación también falló para cita={}: {}", pago.getIdCita(), ex.getMessage());
            }
            return toResponse(pago);
        }

        // Publicar PagoConsultaConfirmado hacia RabbitMQ (ms-notificaciones escucha)
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_CAJA,
                RabbitMQConfig.ROUTING_KEY_PAGO_CONFIRMADO,
                new PagoConsultaConfirmadoEvent(pago.getIdCita(), pago.getIdPaciente(), pago.getMonto(), pago.getCorreoPaciente()));
        log.info("Saga 14.1 completada: cita={} CONFIRMADA, evento PagoConsultaConfirmado publicado", pago.getIdCita());

        return toResponse(pago);
    }

    private boolean intentarConfirmarCita(Long idCita) {
        for (int intento = 1; intento <= MAX_REINTENTOS_CONFIRMAR; intento++) {
            try {
                citasClient.actualizarEstado(idCita, new EstadoCitaUpdateDTO("CONFIRMADA"));
                return true;
            } catch (FeignException ex) {
                log.warn("Reintento {}/{} al confirmar cita={}: {}", intento, MAX_REINTENTOS_CONFIRMAR, idCita, ex.getMessage());
            }
        }
        return false;
    }

    private PagoConsultaResponseDTO toResponse(PagoConsulta p) {
        PagoConsultaResponseDTO dto = new PagoConsultaResponseDTO();
        dto.setId(p.getId()); dto.setIdCita(p.getIdCita());
        dto.setIdPaciente(p.getIdPaciente()); dto.setMonto(p.getMonto());
        dto.setEstado(p.getEstado());
        return dto;
    }
}
