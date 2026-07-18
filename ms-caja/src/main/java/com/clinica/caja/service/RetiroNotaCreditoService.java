package com.clinica.caja.service;

import com.clinica.caja.client.AuditoriaClient;
import com.clinica.caja.config.RabbitMQConfig;
import com.clinica.caja.dto.AccionAuditoriaDTO;
import com.clinica.caja.dto.RetiroRequestDTO;
import com.clinica.caja.dto.RetiroResponseDTO;
import com.clinica.caja.event.RetiroSolicitadoEvent;
import com.clinica.caja.model.EstadoNotaCredito;
import com.clinica.caja.model.EstadoRetiro;
import com.clinica.caja.model.NotaCredito;
import com.clinica.caja.model.RetiroNotaCredito;
import com.clinica.caja.repository.NotaCreditoRepository;
import com.clinica.caja.repository.RetiroNotaCreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetiroNotaCreditoService {

    private static final String MODULO = "CAJA";

    private final RetiroNotaCreditoRepository retiroRepository;
    private final NotaCreditoRepository notaCreditoRepository;
    private final RabbitTemplate rabbitTemplate;
    private final AuditoriaClient auditoriaClient;

    @Transactional
    public RetiroResponseDTO solicitarRetiro(Long idPaciente, RetiroRequestDTO request, String authHeader) {
        NotaCredito nc = notaCreditoRepository.findById(request.getIdNotaCredito())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Nota de crédito no encontrada: " + request.getIdNotaCredito()));

        if (!nc.getIdPaciente().equals(idPaciente)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La nota de crédito no pertenece a este paciente.");
        }
        if (nc.getEstado() != EstadoNotaCredito.DISPONIBLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La nota de crédito no está disponible (estado: " + nc.getEstado() + ").");
        }
        if (retiroRepository.existsByIdNotaCredito(nc.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una solicitud de retiro para esta nota de crédito.");
        }

        nc.setEstado(EstadoNotaCredito.USADA);
        notaCreditoRepository.save(nc);

        RetiroNotaCredito retiro = new RetiroNotaCredito();
        retiro.setIdNotaCredito(nc.getId());
        retiro.setIdPaciente(idPaciente);
        retiro.setMonto(nc.getMonto());
        retiro.setNombreBanco(request.getNombreBanco());
        retiro.setNumeroCuenta(request.getNumeroCuenta());
        retiro.setNombreTitular(request.getNombreTitular());
        retiro.setEstado(EstadoRetiro.SOLICITADO);
        retiro.setFechaSolicitud(LocalDateTime.now());
        retiro.setCorreoPaciente(request.getCorreoConfirmacion());

        RetiroNotaCredito saved = retiroRepository.save(retiro);

        if (request.getCorreoConfirmacion() != null && !request.getCorreoConfirmacion().isBlank()) {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_CAJA,
                    RabbitMQConfig.ROUTING_KEY_RETIRO_SOLICITADO,
                    new RetiroSolicitadoEvent(
                            saved.getId(), idPaciente, request.getNombreTitular(),
                            request.getCorreoConfirmacion(), nc.getMonto(),
                            request.getNombreBanco(), request.getNumeroCuenta(),
                            saved.getFechaSolicitud()),
                    withCorrelationId());
            log.info("RetiroSolicitado publicado: retiro={}", saved.getId());
            auditarAsync("MSG_ENCOLADO", "RetiroNotaCredito", String.valueOf(saved.getId()),
                    "EXITO", RabbitMQConfig.ROUTING_KEY_RETIRO_SOLICITADO, authHeader, null);
        }

        return toResponse(saved);
    }

    private MessagePostProcessor withCorrelationId() {
        String cid = MDC.get("correlationId");
        return msg -> {
            if (cid != null) msg.getMessageProperties().setCorrelationId(cid);
            return msg;
        };
    }

    private void auditarAsync(String accion, String entidadTipo, String entidadId,
                               String resultado, String disparaEvento,
                               String authHeader, String metadatos) {
        String cid = MDC.get("correlationId");
        long timestampMs = System.currentTimeMillis();
        CompletableFuture.runAsync(() -> {
            try {
                auditoriaClient.registrar(
                        AccionAuditoriaDTO.builder()
                                .modulo(MODULO).accion(accion).entidadTipo(entidadTipo)
                                .entidadId(entidadId).resultado(resultado).correlationId(cid)
                                .disparaEvento(disparaEvento).metadatos(metadatos)
                                .timestamp(timestampMs).build(),
                        authHeader);
            } catch (Exception e) {
                log.warn("ACTION_LOG no registrado [{}/{}]: {}", accion, entidadId, e.getMessage());
            }
        });
    }

    @Transactional(readOnly = true)
    public List<RetiroResponseDTO> listarPorPaciente(Long idPaciente) {
        return retiroRepository.findByIdPaciente(idPaciente).stream()
                .map(this::toResponse)
                .toList();
    }

    private RetiroResponseDTO toResponse(RetiroNotaCredito r) {
        RetiroResponseDTO dto = new RetiroResponseDTO();
        dto.setId(r.getId());
        dto.setIdNotaCredito(r.getIdNotaCredito());
        dto.setIdPaciente(r.getIdPaciente());
        dto.setMonto(r.getMonto());
        dto.setNombreBanco(r.getNombreBanco());
        dto.setNumeroCuenta(r.getNumeroCuenta());
        dto.setNombreTitular(r.getNombreTitular());
        dto.setEstado(r.getEstado());
        dto.setFechaSolicitud(r.getFechaSolicitud());
        return dto;
    }
}
