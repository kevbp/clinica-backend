package com.clinica.caja.service;

import com.clinica.caja.config.RabbitMQConfig;
import com.clinica.caja.dto.ComprobanteResponseDTO;
import com.clinica.caja.event.ReenviarComprobanteEvent;
import com.clinica.caja.model.Comprobante;
import com.clinica.caja.model.TipoComprobante;
import com.clinica.caja.repository.ComprobanteRepository;
import com.clinica.caja.repository.PagoConsultaRepository;
import com.clinica.caja.repository.ProformaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

// Comprobante no guarda idPaciente directamente (su idOrigen es un PagoConsulta o una Proforma,
// cada uno dueño de esa referencia) — para listar por paciente se cruza primero con esos dos repos.
@Slf4j
@Service
@RequiredArgsConstructor
public class ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;
    private final PagoConsultaRepository pagoConsultaRepository;
    private final ProformaRepository proformaRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional(readOnly = true)
    public List<ComprobanteResponseDTO> listarPorPaciente(Long idPaciente) {
        List<Long> idsPagosConsulta = pagoConsultaRepository.findByIdPaciente(idPaciente).stream()
                .map(p -> p.getId()).toList();
        List<Long> idsProformas = proformaRepository.findByIdPaciente(idPaciente).stream()
                .map(p -> p.getId()).toList();

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

    @Transactional(readOnly = true)
    public void enviarPorCorreo(Long id, String correo) {
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
                evento
        );
        log.info("ReenviarComprobante publicado: numero={} correo={}", c.getNumero(), correo);
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
        return dto;
    }
}
