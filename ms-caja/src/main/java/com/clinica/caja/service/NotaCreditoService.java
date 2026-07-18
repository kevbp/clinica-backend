package com.clinica.caja.service;

import com.clinica.caja.client.AuditoriaClient;
import com.clinica.caja.config.RabbitMQConfig;
import com.clinica.caja.dto.AccionAuditoriaDTO;
import com.clinica.caja.dto.EnviarCorreoRequestDTO;
import com.clinica.caja.dto.NotaCreditoRequestDTO;
import com.clinica.caja.dto.NotaCreditoResponseDTO;
import com.clinica.caja.event.ReenviarNotaCreditoEvent;
import com.clinica.caja.model.EstadoNotaCredito;
import com.clinica.caja.model.EstadoPagoConsulta;
import com.clinica.caja.model.NotaCredito;
import com.clinica.caja.model.PagoConsulta;
import com.clinica.caja.model.TipoComprobante;
import com.clinica.caja.model.TipoNotaCredito;
import com.clinica.caja.repository.ComprobanteRepository;
import com.clinica.caja.repository.NotaCreditoRepository;
import com.clinica.caja.repository.PagoConsultaRepository;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotaCreditoService {

    private static final Logger log = LoggerFactory.getLogger(NotaCreditoService.class);

    private static final BigDecimal PORCENTAJE_DEVOLUCION_PARCIAL = new BigDecimal("0.70");
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String MODULO = "CAJA";

    private final NotaCreditoRepository notaCreditoRepository;
    private final PagoConsultaRepository pagoRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final RabbitTemplate rabbitTemplate;
    private final AuditoriaClient auditoriaClient;

    @Transactional(readOnly = true)
    public List<NotaCreditoResponseDTO> listarPorPaciente(Long idPaciente) {
        return notaCreditoRepository.findByIdPaciente(idPaciente).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotaCreditoResponseDTO emitir(NotaCreditoRequestDTO request) {
        PagoConsulta pago = pagoRepository.findByIdCita(request.getIdCita())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe pago de consulta para la cita " + request.getIdCita()));

        if (pago.getEstado() != EstadoPagoConsulta.PAGADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede emitir una nota de crédito sobre un pago en estado PAGADO.");
        }

        BigDecimal creditoAplicado = pago.getMontoCreditoAplicado() != null
                ? pago.getMontoCreditoAplicado() : BigDecimal.ZERO;
        BigDecimal montoTotal = pago.getMonto().subtract(creditoAplicado).max(BigDecimal.ZERO);
        boolean conPenalidad = (request.getTipo() == TipoNotaCredito.CANCELACION_TARDIA
                || request.getTipo() == TipoNotaCredito.NO_SHOW);

        BigDecimal montoDevolucion;
        BigDecimal montoRetenido;
        if (conPenalidad) {
            montoDevolucion = montoTotal.multiply(PORCENTAJE_DEVOLUCION_PARCIAL)
                    .setScale(2, RoundingMode.HALF_UP);
            montoRetenido = montoTotal.subtract(montoDevolucion);
        } else {
            montoDevolucion = montoTotal;
            montoRetenido = BigDecimal.ZERO.setScale(2);
        }

        NotaCredito nc = new NotaCredito();
        nc.setIdPaciente(pago.getIdPaciente());
        nc.setMonto(montoDevolucion);
        nc.setMontoRetenido(montoRetenido);
        nc.setIdPagoConsultaOrigen(pago.getId());
        nc.setMotivo(request.getMotivo());
        nc.setTipo(request.getTipo());
        nc.setEstado(EstadoNotaCredito.DISPONIBLE);

        // Vincular al comprobante original si ya fue emitido
        comprobanteRepository.findByTipoAndIdOrigen(TipoComprobante.CONSULTA, pago.getId())
                .ifPresent(comp -> nc.setIdComprobanteRelacionado(comp.getId()));

        NotaCredito saved = notaCreditoRepository.save(nc);
        // Número correlativo formal para SUNAT: NC-YYYYMMDD-{id con ceros}
        saved.setNumero(String.format("NC-%s-%05d", LocalDate.now().format(FMT_FECHA), saved.getId()));
        return toResponse(notaCreditoRepository.save(saved));
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerSaldoDisponible(Long idPaciente) {
        return notaCreditoRepository.findByIdPacienteAndEstado(idPaciente, EstadoNotaCredito.DISPONIBLE)
                .stream()
                .map(NotaCredito::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void enviarPorCorreo(Long idNc, String correo, String authHeader) {
        NotaCredito nc = notaCreditoRepository.findById(idNc)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "NC no encontrada: " + idNc));

        ReenviarNotaCreditoEvent evento = new ReenviarNotaCreditoEvent(
                nc.getId(), nc.getNumero(), correo,
                nc.getTipo().name(), nc.getMonto(), nc.getMontoRetenido(),
                nc.getMotivo(), LocalDate.now().atStartOfDay());

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_CAJA, RabbitMQConfig.ROUTING_KEY_REENVIAR_NC, evento, withCorrelationId());
        log.info("ReenviarNC publicado: numero={}", nc.getNumero());
        auditarAsync("MSG_ENCOLADO", "NotaCredito", String.valueOf(idNc),
                "EXITO", RabbitMQConfig.ROUTING_KEY_REENVIAR_NC, authHeader, null);
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

    private NotaCreditoResponseDTO toResponse(NotaCredito nc) {
        NotaCreditoResponseDTO dto = new NotaCreditoResponseDTO();
        dto.setId(nc.getId());
        dto.setNumero(nc.getNumero());
        dto.setIdPaciente(nc.getIdPaciente());
        dto.setTipo(nc.getTipo());
        dto.setMonto(nc.getMonto());
        dto.setMontoRetenido(nc.getMontoRetenido());
        dto.setIdPagoConsultaOrigen(nc.getIdPagoConsultaOrigen());
        dto.setMotivo(nc.getMotivo());
        dto.setEstado(nc.getEstado());
        dto.setIdComprobanteRelacionado(nc.getIdComprobanteRelacionado());
        return dto;
    }
}
