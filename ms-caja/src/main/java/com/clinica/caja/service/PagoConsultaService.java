package com.clinica.caja.service;

import com.clinica.caja.client.AuditoriaClient;
import com.clinica.caja.client.CitasFeignClient;
import com.clinica.caja.client.PersonalFeignClient;
import com.clinica.caja.dto.AccionAuditoriaDTO;
import com.clinica.caja.client.dto.EstadoCitaEnum;
import com.clinica.caja.client.dto.EstadoCitaUpdateDTO;
import com.clinica.caja.client.dto.PersonalDTO;
import com.clinica.caja.config.RabbitMQConfig;
import com.clinica.caja.dto.ComprobanteResponseDTO;
import com.clinica.caja.dto.PagoConsultaRequestDTO;
import com.clinica.caja.dto.PagoConsultaResponseDTO;
import com.clinica.caja.event.PagoConsultaConfirmadoEvent;
import com.clinica.caja.model.Comprobante;
import com.clinica.caja.model.EstadoPagoConsulta;
import com.clinica.caja.model.PagoConsulta;
import com.clinica.caja.model.TarifaConsulta;
import com.clinica.caja.model.TipoComprobante;
import com.clinica.caja.model.EstadoNotaCredito;
import com.clinica.caja.model.NotaCredito;
import com.clinica.caja.repository.ComprobanteRepository;
import com.clinica.caja.repository.NotaCreditoRepository;
import com.clinica.caja.repository.PagoConsultaRepository;
import com.clinica.caja.repository.TarifaConsultaRepository;
import feign.FeignException;
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
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PagoConsultaService {

    private static final Logger log = LoggerFactory.getLogger(PagoConsultaService.class);

    private static final int    MAX_REINTENTOS_CONFIRMAR = 3;
    private static final String MODULO                  = "CAJA";

    private final PagoConsultaRepository    pagoRepository;
    private final TarifaConsultaRepository  tarifaRepository;
    private final ComprobanteRepository     comprobanteRepository;
    private final NotaCreditoRepository     notaCreditoRepository;
    private final PersonalFeignClient       personalClient;
    private final CitasFeignClient          citasClient;
    private final RabbitTemplate            rabbitTemplate;
    private final AuditoriaClient           auditoriaClient;

    @Transactional(readOnly = true)
    public PagoConsultaResponseDTO obtenerPorId(Long id) {
        return toResponse(pagoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "PagoConsulta no encontrado con id: " + id)));
    }

    @Transactional(readOnly = true)
    public PagoConsultaResponseDTO obtenerPorCita(Long idCita) {
        return toResponse(pagoRepository.findByIdCita(idCita)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe pago de consulta para la cita: " + idCita)));
    }

    @Transactional
    public PagoConsultaResponseDTO crear(PagoConsultaRequestDTO request, String authHeader) {
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
        String nombreEspecialidad = personal.getMedicoInfo().getEspecialidad().getNombre();
        TarifaConsulta tarifa = tarifaRepository.findById(idEspecialidad)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe tarifa de consulta para la especialidad " + idEspecialidad));

        PagoConsulta pago = new PagoConsulta();
        pago.setIdCita(request.getIdCita());
        pago.setIdPaciente(request.getIdPaciente());
        pago.setMonto(tarifa.getMonto());
        pago.setCorreoPaciente(request.getCorreoPaciente());
        pago.setNombrePaciente(request.getNombrePaciente());
        pago.setEspecialidad(nombreEspecialidad);
        pago.setEstado(EstadoPagoConsulta.PENDIENTE);

        PagoConsulta saved = pagoRepository.save(pago);

        auditarAsync("REGISTRAR_COBRO_CONSULTA", "PagoConsulta", String.valueOf(saved.getId()),
                "EXITO", null, authHeader,
                "{\"idCita\":" + saved.getIdCita() + ",\"idPaciente\":" + saved.getIdPaciente() + "}");

        return toResponse(saved);
    }

    /**
     * Saga 14.1 — Confirmación de pago de consulta.
     * Orden: (1) marcar PAGADO local → (2) confirmar cita con reintentos
     *        → compensación si falla → (3) publicar evento si todo sale bien.
     */
    @Transactional
    public PagoConsultaResponseDTO confirmarPago(Long id, String authHeader) {
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
        long tPago = System.currentTimeMillis();

        auditarAsync("CONFIRMAR_PAGO_CONSULTA", "PagoConsulta", String.valueOf(pago.getId()),
                "EXITO", "PagoConsultaConfirmado", authHeader,
                "{\"idCita\":" + pago.getIdCita() + ",\"idPaciente\":" + pago.getIdPaciente() + "}", tPago);

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

        auditarAsync("CONFIRMAR_CITA", "CitaMedica", String.valueOf(pago.getIdCita()),
                "EXITO", null, authHeader,
                "{\"idPaciente\":" + pago.getIdPaciente() + "}");

        // Boleta automática: el monto real cobrado es monto - crédito aplicado
        BigDecimal credito = pago.getMontoCreditoAplicado() != null ? pago.getMontoCreditoAplicado() : BigDecimal.ZERO;
        BigDecimal montoACobrar = pago.getMonto().subtract(credito).max(BigDecimal.ZERO);
        BigDecimal subtotal = calcularSubtotal(montoACobrar);
        LocalDateTime fechaEmision = LocalDateTime.now();

        Comprobante comprobante = new Comprobante();
        comprobante.setTipo(TipoComprobante.CONSULTA);
        comprobante.setIdOrigen(pago.getId());
        comprobante.setIdCita(pago.getIdCita());
        comprobante.setEspecialidad(pago.getEspecialidad());
        comprobante.setSubtotal(subtotal);
        comprobante.setIgv(montoACobrar.subtract(subtotal));
        comprobante.setMontoTotal(montoACobrar);
        comprobante.setFechaEmision(fechaEmision);
        comprobante.setNumero("BC-" + System.currentTimeMillis());
        if (credito.compareTo(BigDecimal.ZERO) > 0) {
            comprobante.setDescuento(credito);
            comprobante.setConceptoDescuento("Nota de Credito aplicada");
        }
        comprobanteRepository.save(comprobante);
        log.info("Boleta {} emitida para pago de consulta id={}", comprobante.getNumero(), pago.getId());

        auditarAsync("EMITIR_COMPROBANTE", "Comprobante", String.valueOf(comprobante.getId()),
                "EXITO", null, authHeader,
                "{\"numero\":\"" + comprobante.getNumero() + "\",\"idCita\":" + pago.getIdCita() + "}");

        // Publicar PagoConsultaConfirmado hacia RabbitMQ (ms-notificaciones escucha)
        PagoConsultaConfirmadoEvent evento = new PagoConsultaConfirmadoEvent(
                pago.getIdCita(), pago.getIdPaciente(), pago.getMonto(),
                pago.getCorreoPaciente(), pago.getNombrePaciente(),
                comprobante.getNumero(), comprobante.getSubtotal(), comprobante.getIgv(),
                comprobante.getMontoTotal(), comprobante.getDescuento(), comprobante.getConceptoDescuento(),
                fechaEmision, pago.getEspecialidad());
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_CAJA,
                    RabbitMQConfig.ROUTING_KEY_PAGO_CONFIRMADO,
                    evento, withCorrelationId());
            log.info("Saga 14.1 completada: cita={} CONFIRMADA, evento PagoConsultaConfirmado publicado", pago.getIdCita());
            auditarAsync("MSG_ENCOLADO", "PagoConsultaConfirmado", String.valueOf(pago.getId()),
                    "EXITO", RabbitMQConfig.ROUTING_KEY_PAGO_CONFIRMADO, authHeader, null);
        } catch (Exception ex) {
            log.error("Error al publicar PagoConsultaConfirmado para pago={}: {}", pago.getId(), ex.getMessage());
            auditarAsyncError("MSG_ERROR_ENCOLAR", "PagoConsultaConfirmado", String.valueOf(pago.getId()),
                    RabbitMQConfig.ROUTING_KEY_PAGO_CONFIRMADO, authHeader, ex.getMessage());
        }

        return toResponse(pago);
    }

    private void auditarAsync(String accion, String entidadTipo, String entidadId,
                               String resultado, String disparaEvento,
                               String authHeader, String metadatos) {
        auditarAsync(accion, entidadTipo, entidadId, resultado, disparaEvento, authHeader, metadatos,
                     System.currentTimeMillis());
    }

    private void auditarAsync(String accion, String entidadTipo, String entidadId,
                               String resultado, String disparaEvento,
                               String authHeader, String metadatos, long timestampMs) {
        String cid = MDC.get("correlationId");
        CompletableFuture.runAsync(() -> {
            try {
                auditoriaClient.registrar(
                        AccionAuditoriaDTO.builder()
                                .modulo(MODULO)
                                .accion(accion)
                                .entidadTipo(entidadTipo)
                                .entidadId(entidadId)
                                .resultado(resultado)
                                .correlationId(cid)
                                .disparaEvento(disparaEvento)
                                .metadatos(metadatos)
                                .timestamp(timestampMs)
                                .build(),
                        authHeader);
            } catch (Exception e) {
                log.warn("ACTION_LOG no registrado [{}/{}]: {}", accion, entidadId, e.getMessage());
            }
        });
    }

    private void auditarAsyncError(String accion, String entidadTipo, String entidadId,
                                   String disparaEvento, String authHeader, String errorDetalle) {
        String cid = MDC.get("correlationId");
        long ts = System.currentTimeMillis();
        CompletableFuture.runAsync(() -> {
            try {
                auditoriaClient.registrar(
                        AccionAuditoriaDTO.builder()
                                .modulo(MODULO)
                                .accion(accion)
                                .entidadTipo(entidadTipo)
                                .entidadId(entidadId)
                                .resultado("ERROR")
                                .correlationId(cid)
                                .disparaEvento(disparaEvento)
                                .errorDetalle(errorDetalle)
                                .timestamp(ts)
                                .build(),
                        authHeader);
            } catch (Exception e) {
                log.warn("ACTION_LOG no registrado [{}/{}]: {}", accion, entidadId, e.getMessage());
            }
        });
    }

    private MessagePostProcessor withCorrelationId() {
        String cid = MDC.get("correlationId");
        return msg -> {
            if (cid != null) msg.getMessageProperties().setCorrelationId(cid);
            return msg;
        };
    }

    @Transactional(readOnly = true)
    public ComprobanteResponseDTO obtenerComprobante(Long idPago) {
        Comprobante comprobante = comprobanteRepository.findByTipoAndIdOrigen(TipoComprobante.CONSULTA, idPago)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aún no se ha emitido boleta para el pago de consulta id: " + idPago));
        return toComprobanteResponse(comprobante);
    }

    private ComprobanteResponseDTO toComprobanteResponse(Comprobante c) {
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

    // IGV peruano: precio almacenado ya incluye el 18%.
    // subtotal = total / 1.18 redondeado a 2 decimales; igv = total - subtotal.
    private static BigDecimal calcularSubtotal(BigDecimal total) {
        return total.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
    }

    private boolean intentarConfirmarCita(Long idCita) {
        for (int intento = 1; intento <= MAX_REINTENTOS_CONFIRMAR; intento++) {
            try {
                citasClient.actualizarEstado(idCita, new EstadoCitaUpdateDTO(EstadoCitaEnum.CONFIRMADA));
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
        BigDecimal credito = p.getMontoCreditoAplicado() != null ? p.getMontoCreditoAplicado() : BigDecimal.ZERO;
        dto.setMontoCreditoAplicado(credito);
        dto.setMontoACobrar(p.getMonto().subtract(credito).max(BigDecimal.ZERO));
        return dto;
    }

    @Transactional
    public PagoConsultaResponseDTO aplicarCredito(Long idPago, Long idNotaCredito) {
        PagoConsulta pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PagoConsulta no encontrado: " + idPago));
        if (pago.getEstado() != EstadoPagoConsulta.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede aplicar crédito a un pago PENDIENTE.");
        }
        NotaCredito nc = notaCreditoRepository.findById(idNotaCredito)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nota de crédito no encontrada: " + idNotaCredito));
        if (!nc.getIdPaciente().equals(pago.getIdPaciente())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La nota de crédito no pertenece al paciente del pago.");
        }
        if (nc.getEstado() != EstadoNotaCredito.DISPONIBLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La nota de crédito no está disponible (estado: " + nc.getEstado() + ").");
        }
        BigDecimal creditoActual = pago.getMontoCreditoAplicado() != null ? pago.getMontoCreditoAplicado() : BigDecimal.ZERO;
        BigDecimal nuevoCreditoTotal = creditoActual.add(nc.getMonto()).min(pago.getMonto());
        pago.setMontoCreditoAplicado(nuevoCreditoTotal);
        nc.setEstado(EstadoNotaCredito.USADA);
        notaCreditoRepository.save(nc);
        return toResponse(pagoRepository.save(pago));
    }
}
