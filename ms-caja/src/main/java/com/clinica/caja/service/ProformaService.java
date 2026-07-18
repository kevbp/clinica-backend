package com.clinica.caja.service;

import com.clinica.caja.client.AuditoriaClient;
import com.clinica.caja.client.FarmaciaFeignClient;
import com.clinica.caja.client.HistoriasFeignClient;
import com.clinica.caja.client.LaboratorioFeignClient;
import com.clinica.caja.client.PacientesFeignClient;
import com.clinica.caja.config.RabbitMQConfig;
import com.clinica.caja.dto.AccionAuditoriaDTO;
import com.clinica.caja.client.dto.*;
import com.clinica.caja.event.ReenviarComprobanteEvent;
import com.clinica.caja.dto.ComprobanteResponseDTO;
import com.clinica.caja.dto.ConstruirProformaRequestDTO;
import com.clinica.caja.dto.ItemProformaResponseDTO;
import com.clinica.caja.dto.PagarItemsRequestDTO;
import com.clinica.caja.dto.ProformaResponseDTO;
import com.clinica.caja.model.*;
import com.clinica.caja.repository.ComprobanteRepository;
import com.clinica.caja.repository.ItemProformaRepository;
import com.clinica.caja.repository.ProformaRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.slf4j.MDC;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProformaService {

    private static final int    DIAS_VIGENCIA = 7;
    private static final String MODULO        = "CAJA";

    private final ProformaRepository       proformaRepository;
    private final ItemProformaRepository   itemRepository;
    private final ComprobanteRepository    comprobanteRepository;
    private final HistoriasFeignClient     historiasClient;
    private final FarmaciaFeignClient      farmaciaClient;
    private final LaboratorioFeignClient   laboratorioClient;
    private final PacientesFeignClient     pacientesClient;
    private final AuditoriaClient          auditoriaClient;
    private final RabbitTemplate           rabbitTemplate;

    @Transactional(readOnly = true)
    public ProformaResponseDTO obtenerPorId(Long id) {
        return toResponse(proformaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Proforma no encontrada con id: " + id)));
    }

    @Transactional(readOnly = true)
    public List<ProformaResponseDTO> listarPorPaciente(Long idPaciente) {
        return proformaRepository.findByIdPaciente(idPaciente).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProformaResponseDTO> listarPorReceta(String idReceta) {
        return proformaRepository.findByIdReceta(idReceta).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProformaResponseDTO> listarPorOrden(String idOrden) {
        return proformaRepository.findByIdOrden(idOrden).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProformaResponseDTO construirDesdeReceta(String idReceta, ConstruirProformaRequestDTO request) {
        RecetaDTO receta = historiasClient.obtenerRecetaPorId(idReceta).getBody();
        if (receta == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Receta no encontrada: " + idReceta);
        }
        if (receta.getLineas() == null || receta.getLineas().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La receta no tiene medicamentos prescritos.");
        }

        List<LineaRecetaDTO> lineasSeleccionadas = receta.getLineas().stream()
                .filter(l -> request.getIdsItemsSeleccionados().contains(l.getIdMedicamento()))
                .toList();
        if (lineasSeleccionadas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ninguno de los medicamentos seleccionados está en la receta.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        Proforma proforma = new Proforma();
        proforma.setIdPaciente(receta.getIdPaciente());
        proforma.setIdReceta(idReceta);
        proforma.setTipo(TipoProforma.MEDICAMENTOS);
        proforma.setFechaGeneracion(ahora);
        proforma.setFechaVigencia(ahora.plusDays(DIAS_VIGENCIA));
        proforma = proformaRepository.save(proforma);

        List<ItemProforma> items = new ArrayList<>();
        for (LineaRecetaDTO linea : lineasSeleccionadas) {
            try {
                PrecioMedicamentoDTO precio = farmaciaClient.obtenerPrecio(linea.getIdMedicamento()).getBody();
                if (precio == null) continue;
                BigDecimal unitario = precio.getPrecio().setScale(2, RoundingMode.HALF_UP);
                int cantidad = linea.getCantidadTotal() != null ? linea.getCantidadTotal() : 1;
                ItemProforma item = new ItemProforma();
                item.setProforma(proforma);
                item.setTipo(TipoItem.MEDICAMENTO);
                item.setIdItem(linea.getIdMedicamento());
                item.setNombreItem(linea.getNombreMedicamento());
                item.setPrincipioActivo(linea.getPrincipioActivo());
                item.setPresentacion(linea.getPresentacion());
                item.setDosis(linea.getDosis());
                item.setFrecuencia(linea.getFrecuencia());
                item.setDuracion(linea.getDuracion());
                item.setPrecioUnitario(unitario);
                item.setPrecioCongelado(unitario.multiply(BigDecimal.valueOf(cantidad)));
                item.setCantidad(cantidad);
                item.setEstado(EstadoItem.PENDIENTE);
                items.add(item);
            } catch (FeignException ex) {
                log.warn("No se pudo obtener precio del medicamento {}: {}", linea.getIdMedicamento(), ex.getMessage());
            }
        }

        if (items.isEmpty()) {
            proformaRepository.delete(proforma);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo obtener precio de ningun medicamento.");
        }

        itemRepository.saveAll(items);
        proforma.setItems(items);
        return toResponse(proforma);
    }

    @Transactional
    public ProformaResponseDTO construirDesdeOrden(String idOrden, ConstruirProformaRequestDTO request) {
        OrdenDTO orden = historiasClient.obtenerOrdenPorId(idOrden).getBody();
        if (orden == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada: " + idOrden);
        }
        if (orden.getLineas() == null || orden.getLineas().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La orden no tiene examenes solicitados.");
        }

        List<LineaOrdenDTO> lineasSeleccionadas = orden.getLineas().stream()
                .filter(l -> request.getIdsItemsSeleccionados().contains(l.getIdExamen()))
                .toList();
        if (lineasSeleccionadas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ninguno de los exámenes seleccionados está en la orden.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        Proforma proforma = new Proforma();
        proforma.setIdPaciente(orden.getIdPaciente());
        proforma.setIdOrden(idOrden);
        proforma.setTipo(TipoProforma.EXAMENES);
        proforma.setFechaGeneracion(ahora);
        proforma.setFechaVigencia(ahora.plusDays(DIAS_VIGENCIA));
        proforma = proformaRepository.save(proforma);

        List<ItemProforma> items = new ArrayList<>();
        for (LineaOrdenDTO linea : lineasSeleccionadas) {
            try {
                PrecioExamenDTO precio = laboratorioClient.obtenerPrecio(linea.getIdExamen()).getBody();
                if (precio == null) continue;
                BigDecimal unitario = precio.getPrecio().setScale(2, RoundingMode.HALF_UP);
                ItemProforma item = new ItemProforma();
                item.setProforma(proforma);
                item.setTipo(TipoItem.EXAMEN);
                item.setIdItem(linea.getIdExamen());
                item.setIdEpisodioClinico(orden.getIdEpisodioClinico());
                item.setNombreItem(linea.getNombreExamen());
                item.setCategoria(linea.getCategoria());
                item.setIndicacionesPreparacion(linea.getIndicacionesPreparacion());
                item.setCantidad(1);
                item.setPrecioUnitario(unitario);
                item.setPrecioCongelado(unitario);
                item.setEstado(EstadoItem.PENDIENTE);
                items.add(item);
            } catch (FeignException ex) {
                log.warn("No se pudo obtener precio del examen {}: {}", linea.getIdExamen(), ex.getMessage());
            }
        }

        if (items.isEmpty()) {
            proformaRepository.delete(proforma);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo obtener precio de ningun examen.");
        }

        itemRepository.saveAll(items);
        proforma.setItems(items);
        return toResponse(proforma);
    }

    @Transactional
    public ProformaResponseDTO pagarItems(Long idProforma, PagarItemsRequestDTO request, String authHeader) {
        Proforma proforma = proformaRepository.findById(idProforma)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proforma no encontrada: " + idProforma));

        EstadoProforma estado = calcularEstado(proforma);
        if (estado == EstadoProforma.EXPIRADA) {
            throw new ResponseStatusException(HttpStatus.GONE,
                    "La proforma expiro el " + proforma.getFechaVigencia() + ". Genere una nueva desde la receta u orden.");
        }
        if (estado == EstadoProforma.PAGADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La proforma ya fue completamente pagada.");
        }

        List<ItemProforma> itemsAPagar = proforma.getItems().stream()
                .filter(i -> request.getIdsItems().contains(i.getId()) && i.getEstado() == EstadoItem.PENDIENTE)
                .toList();

        String refProforma = "PRF-" + proforma.getId();
        for (ItemProforma item : itemsAPagar) {
            if (item.getTipo() == TipoItem.MEDICAMENTO) {
                procesarMedicamento(item, refProforma);
            } else {
                procesarExamen(item, proforma.getIdPaciente());
            }
        }

        long tPago = System.currentTimeMillis();
        auditarAsync("PAGAR_ITEMS_PROFORMA", "Proforma", String.valueOf(idProforma),
                "EXITO", null, authHeader,
                "{\"tipo\":\"" + proforma.getTipo() + "\"}", tPago);

        return toResponse(proforma);
    }

    private void procesarMedicamento(ItemProforma item, String referencia) {
        try {
            DescontarStockResponseDTO result = farmaciaClient
                    .descontarStock(item.getIdItem(), new DescontarStockRequestDTO(item.getCantidad(), referencia))
                    .getBody();
            if (result != null && result.isExitoso()) {
                item.setEstado(EstadoItem.PAGADO);
            } else {
                item.setEstado(EstadoItem.NO_DISPONIBLE);
                log.warn("Saga 14.2 - medicamento {} sin stock suficiente", item.getIdItem());
            }
        } catch (FeignException ex) {
            log.warn("Saga 14.2 - fallo tecnico al descontar stock medicamento {}: {}", item.getIdItem(), ex.getMessage());
        }
    }

    private void procesarExamen(ItemProforma item, Long idPaciente) {
        try {
            laboratorioClient.autorizarExamen(
                    new ExamenAutorizadoRequestDTO(idPaciente, item.getIdEpisodioClinico(), item.getIdItem()));
            item.setEstado(EstadoItem.PAGADO);
        } catch (FeignException ex) {
            if (ex.status() >= 400 && ex.status() < 500) {
                item.setEstado(EstadoItem.NO_DISPONIBLE);
            } else {
                log.warn("Saga 14.2 - fallo tecnico al autorizar examen {}: {}", item.getIdItem(), ex.getMessage());
            }
        }
    }

    @Transactional
    public ComprobanteResponseDTO emitirComprobante(Long idProforma, String authHeader) {
        Proforma proforma = proformaRepository.findById(idProforma)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proforma no encontrada: " + idProforma));

        BigDecimal total = proforma.getItems().stream()
                .filter(i -> i.getEstado() == EstadoItem.PAGADO)
                .map(ItemProforma::getPrecioCongelado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(BigDecimal.ZERO) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay items pagados en la proforma.");
        }

        BigDecimal subtotal = total.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
        Comprobante comprobante = new Comprobante();
        comprobante.setTipo(TipoComprobante.PROFORMA);
        comprobante.setIdOrigen(idProforma);
        comprobante.setSubtotal(subtotal);
        comprobante.setIgv(total.subtract(subtotal));
        comprobante.setMontoTotal(total);
        comprobante.setFechaEmision(LocalDateTime.now());
        comprobante.setNumero("BC-" + System.currentTimeMillis());
        comprobante.setIdReceta(proforma.getIdReceta());
        comprobante.setIdOrden(proforma.getIdOrden());
        comprobante = comprobanteRepository.save(comprobante);

        long tEmision = System.currentTimeMillis();
        auditarAsync("EMITIR_COMPROBANTE", "Comprobante", String.valueOf(comprobante.getId()),
                "EXITO", null, authHeader,
                "{\"idProforma\":" + idProforma + "}", tEmision);

        notificarComprobanteProforma(comprobante, proforma.getIdPaciente(), authHeader);

        return toComprobanteResponse(comprobante);
    }

    private void notificarComprobanteProforma(Comprobante comprobante, Long idPaciente, String authHeader) {
        try {
            PacienteDTO paciente = pacientesClient.obtenerPaciente(idPaciente).getBody();
            if (paciente == null || paciente.getCorreo() == null) return;
            ReenviarComprobanteEvent evento = new ReenviarComprobanteEvent(
                    comprobante.getId(),
                    comprobante.getNumero(),
                    paciente.getCorreo(),
                    comprobante.getMontoTotal(),
                    comprobante.getSubtotal(),
                    comprobante.getIgv(),
                    comprobante.getFechaEmision());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_CAJA,
                    RabbitMQConfig.ROUTING_KEY_REENVIAR_COMPROBANTE,
                    evento, withCorrelationId());
            log.info("ComprobanteProforma publicado: numero={}", comprobante.getNumero());
            auditarAsync("MSG_ENCOLADO", "ComprobanteProforma", String.valueOf(comprobante.getId()),
                    "EXITO", RabbitMQConfig.ROUTING_KEY_REENVIAR_COMPROBANTE, authHeader, null);
        } catch (Exception e) {
            log.error("Error al publicar ComprobanteProforma id={}: {}", comprobante.getId(), e.getMessage());
            auditarAsyncError("MSG_ERROR_ENCOLAR", "ComprobanteProforma", String.valueOf(comprobante.getId()),
                    RabbitMQConfig.ROUTING_KEY_REENVIAR_COMPROBANTE, authHeader, e.getMessage());
        }
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

    private EstadoProforma calcularEstado(Proforma p) {
        if (p.getItems() == null || p.getItems().isEmpty()) return EstadoProforma.VIGENTE;
        boolean tienePagados = p.getItems().stream().anyMatch(i -> i.getEstado() == EstadoItem.PAGADO);
        boolean todosFinalizados = p.getItems().stream()
                .allMatch(i -> i.getEstado() == EstadoItem.PAGADO || i.getEstado() == EstadoItem.NO_DISPONIBLE);
        if (tienePagados && todosFinalizados) return EstadoProforma.PAGADA;
        if (LocalDateTime.now().isAfter(p.getFechaVigencia())) return EstadoProforma.EXPIRADA;
        return EstadoProforma.VIGENTE;
    }

    private ProformaResponseDTO toResponse(Proforma p) {
        ProformaResponseDTO dto = new ProformaResponseDTO();
        dto.setId(p.getId());
        dto.setIdPaciente(p.getIdPaciente());
        dto.setIdReceta(p.getIdReceta());
        dto.setIdOrden(p.getIdOrden());
        dto.setTipo(p.getTipo());
        dto.setFechaGeneracion(p.getFechaGeneracion());
        dto.setFechaVigencia(p.getFechaVigencia());
        dto.setEstadoProforma(calcularEstado(p));
        dto.setItems(p.getItems() == null ? List.of() : p.getItems().stream().map(i -> {
            ItemProformaResponseDTO d = new ItemProformaResponseDTO();
            d.setId(i.getId());
            d.setTipo(i.getTipo());
            d.setIdItem(i.getIdItem());
            d.setNombreItem(i.getNombreItem());
            d.setPrincipioActivo(i.getPrincipioActivo());
            d.setPresentacion(i.getPresentacion());
            d.setDosis(i.getDosis());
            d.setFrecuencia(i.getFrecuencia());
            d.setDuracion(i.getDuracion());
            d.setCategoria(i.getCategoria());
            d.setIndicacionesPreparacion(i.getIndicacionesPreparacion());
            d.setPrecioUnitario(i.getPrecioUnitario());
            d.setPrecioCongelado(i.getPrecioCongelado());
            d.setCantidad(i.getCantidad());
            d.setEstado(i.getEstado());
            return d;
        }).toList());
        return dto;
    }

    private ComprobanteResponseDTO toComprobanteResponse(Comprobante c) {
        ComprobanteResponseDTO dto = new ComprobanteResponseDTO();
        dto.setId(c.getId()); dto.setTipo(c.getTipo()); dto.setIdOrigen(c.getIdOrigen());
        dto.setSubtotal(c.getSubtotal()); dto.setIgv(c.getIgv());
        dto.setMontoTotal(c.getMontoTotal()); dto.setFechaEmision(c.getFechaEmision());
        dto.setNumero(c.getNumero());
        return dto;
    }
}