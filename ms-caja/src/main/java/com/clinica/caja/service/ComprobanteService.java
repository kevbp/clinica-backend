package com.clinica.caja.service;

import com.clinica.caja.client.AuditoriaClient;
import com.clinica.caja.config.RabbitMQConfig;
import com.clinica.caja.dto.AccionAuditoriaDTO;
import com.clinica.caja.dto.ComprobanteResponseDTO;
import com.clinica.caja.event.ReenviarComprobanteEvent;
import com.clinica.caja.model.Comprobante;
import com.clinica.caja.model.PagoConsulta;
import com.clinica.caja.model.Proforma;
import com.clinica.caja.model.TipoComprobante;
import com.clinica.caja.repository.ComprobanteRepository;
import com.clinica.caja.repository.PagoConsultaRepository;
import com.clinica.caja.repository.ProformaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

// Comprobante no guarda idPaciente directamente (su idOrigen es un PagoConsulta o una Proforma,
// cada uno dueño de esa referencia) — para listar por paciente se cruza primero con esos dos repos.
@Service
@RequiredArgsConstructor
public class ComprobanteService {

    private static final Logger log = LoggerFactory.getLogger(ComprobanteService.class);

    private static final String MODULO = "CAJA";

    private final ComprobanteRepository comprobanteRepository;
    private final PagoConsultaRepository pagoConsultaRepository;
    private final ProformaRepository proformaRepository;
    private final RabbitTemplate rabbitTemplate;
    private final AuditoriaClient auditoriaClient;

    @Transactional(readOnly = true)
    public List<ComprobanteResponseDTO> listarPorPaciente(Long idPaciente) {
        List<Long> idsPagosConsulta = new java.util.ArrayList<>();
        for (PagoConsulta p : pagoConsultaRepository.findByIdPaciente(idPaciente)) {
            idsPagosConsulta.add(p.getId());
        }
        List<Long> idsProformas = new java.util.ArrayList<>();
        for (com.clinica.caja.model.Proforma p : proformaRepository.findByIdPaciente(idPaciente)) {
            idsProformas.add(p.getId());
        }

        List<Comprobante> comprobantes = new java.util.ArrayList<>();
        if (!idsPagosConsulta.isEmpty()) {
            comprobantes.addAll(comprobanteRepository.findByTipoAndIdOrigenIn(TipoComprobante.CONSULTA, idsPagosConsulta));
        }
        if (!idsProformas.isEmpty()) {
            comprobantes.addAll(comprobanteRepository.findByTipoAndIdOrigenIn(TipoComprobante.PROFORMA, idsProformas));
        }

        return comprobantes.stream()
                .sorted(Comparator.comparing(Comprobante::getFechaEmision).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ComprobanteResponseDTO obtenerPorId(Long id) {
        return toResponse(comprobanteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Comprobante no encontrado con id: " + id)));
    }

    @Transactional
    public void enviarPorCorreo(Long id, String correo, String authHeader) {
        Comprobante c = comprobanteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Comprobante no encontrado con id: " + id));

        ReenviarComprobanteEvent evento = new ReenviarComprobanteEvent(
                c.getId(),
                c.getNumero(),
                correo,
                c.getMontoTotal(),
                c.getSubtotal(),
                c.getIgv(),
                c.getFechaEmision()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_CAJA,
                RabbitMQConfig.ROUTING_KEY_REENVIAR_COMPROBANTE,
                evento, withCorrelationId()
        );
        log.info("ReenviarComprobante publicado: numero={}", c.getNumero());
        auditarAsync("MSG_ENCOLADO", "Comprobante", String.valueOf(id),
                "EXITO", RabbitMQConfig.ROUTING_KEY_REENVIAR_COMPROBANTE, authHeader, null);
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
        java.util.concurrent.CompletableFuture.runAsync(() -> {
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

    private ComprobanteResponseDTO toResponse(Comprobante c) {
        ComprobanteResponseDTO dto = new ComprobanteResponseDTO();
        dto.setId(c.getId());
        dto.setTipo(c.getTipo());
        dto.setIdOrigen(c.getIdOrigen());
        dto.setSubtotal(c.getSubtotal());
        dto.setIgv(c.getIgv());
        dto.setMontoTotal(c.getMontoTotal());
        dto.setFechaEmision(c.getFechaEmision());
        dto.setNumero(c.getNumero());
        dto.setIdCita(c.getIdCita());
        dto.setEspecialidad(c.getEspecialidad());
        dto.setDescuento(c.getDescuento());
        dto.setConceptoDescuento(c.getConceptoDescuento());
        dto.setIdReceta(c.getIdReceta());
        dto.setIdOrden(c.getIdOrden());
        return dto;
    }
}
