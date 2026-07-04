package com.clinica.caja.service;

import com.clinica.caja.client.FarmaciaFeignClient;
import com.clinica.caja.client.HistoriasFeignClient;
import com.clinica.caja.client.LaboratorioFeignClient;
import com.clinica.caja.client.dto.*;
import com.clinica.caja.dto.ComprobanteResponseDTO;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProformaService {

    private final ProformaRepository       proformaRepository;
    private final ItemProformaRepository   itemRepository;
    private final ComprobanteRepository    comprobanteRepository;
    private final HistoriasFeignClient     historiasClient;
    private final FarmaciaFeignClient      farmaciaClient;
    private final LaboratorioFeignClient   laboratorioClient;

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

    /**
     * Construye una nueva proforma congelando precios actuales de los ítems prescritos.
     * El precio queda fijo aunque el catálogo cambie después.
     */
    @Transactional
    public ProformaResponseDTO construir(Long idPaciente) {
        List<RecetaDTO> recetas  = historiasClient.obtenerRecetasPorPaciente(idPaciente).getBody();
        List<OrdenDTO>  ordenes  = historiasClient.obtenerOrdenesPorPaciente(idPaciente).getBody();
        if (recetas == null) recetas = List.of();
        if (ordenes == null) ordenes = List.of();

        Proforma proforma = new Proforma();
        proforma.setIdPaciente(idPaciente);
        proforma.setFechaGeneracion(LocalDateTime.now());
        proforma = proformaRepository.save(proforma);

        List<ItemProforma> items = new ArrayList<>();

        for (RecetaDTO receta : recetas) {
            if (receta.getLineas() == null) continue;
            for (LineaRecetaDTO linea : receta.getLineas()) {
                try {
                    PrecioMedicamentoDTO precio = farmaciaClient.obtenerPrecio(linea.getIdMedicamento()).getBody();
                    if (precio == null) continue;

                    ItemProforma item = new ItemProforma();
                    item.setProforma(proforma);
                    item.setTipo(TipoItem.MEDICAMENTO);
                    item.setIdItem(linea.getIdMedicamento());
                    item.setDescripcion("Medicamento id=" + linea.getIdMedicamento());
                    item.setPrecioCongelado(precio.getPrecio().multiply(BigDecimal.valueOf(linea.getCantidadTotal())));
                    item.setCantidad(linea.getCantidadTotal());
                    item.setEstado(EstadoItem.PENDIENTE);
                    items.add(item);
                } catch (FeignException ex) {
                    log.warn("No se pudo obtener precio de medicamento {}: {}", linea.getIdMedicamento(), ex.getMessage());
                }
            }
        }

        for (OrdenDTO orden : ordenes) {
            if (orden.getLineas() == null) continue;
            for (LineaOrdenDTO linea : orden.getLineas()) {
                try {
                    PrecioExamenDTO precio = laboratorioClient.obtenerPrecio(linea.getIdExamen()).getBody();
                    if (precio == null) continue;

                    ItemProforma item = new ItemProforma();
                    item.setProforma(proforma);
                    item.setTipo(TipoItem.EXAMEN);
                    item.setIdItem(linea.getIdExamen());
                    item.setIdEpisodioClinico(orden.getIdEpisodioClinico());
                    item.setDescripcion("Examen id=" + linea.getIdExamen());
                    item.setPrecioCongelado(precio.getPrecio());
                    item.setEstado(EstadoItem.PENDIENTE);
                    items.add(item);
                } catch (FeignException ex) {
                    log.warn("No se pudo obtener precio de examen {}: {}", linea.getIdExamen(), ex.getMessage());
                }
            }
        }

        itemRepository.saveAll(items);
        proforma.setItems(items);
        return toResponse(proforma);
    }

    /**
     * Saga 14.2 — Pago de ítems de proforma.
     * Cada ítem es una transacción local independiente.
     * Fallo de negocio → NO_DISPONIBLE. Fallo técnico → PENDIENTE (sin afectar los demás).
     */
    @Transactional
    public ProformaResponseDTO pagarItems(Long idProforma, PagarItemsRequestDTO request) {
        Proforma proforma = proformaRepository.findById(idProforma)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Proforma no encontrada con id: " + idProforma));

        List<ItemProforma> itemsAPagar = proforma.getItems().stream()
                .filter(i -> request.getIdsItems().contains(i.getId())
                          && i.getEstado() == EstadoItem.PENDIENTE)
                .toList();

        for (ItemProforma item : itemsAPagar) {
            if (item.getTipo() == TipoItem.MEDICAMENTO) {
                procesarMedicamento(item);
            } else {
                procesarExamen(item, proforma.getIdPaciente());
            }
        }

        return toResponse(proforma);
    }

    private void procesarMedicamento(ItemProforma item) {
        try {
            DescontarStockResponseDTO result = farmaciaClient
                    .descontarStock(item.getIdItem(), new DescontarStockRequestDTO(item.getCantidad()))
                    .getBody();
            if (result != null && result.isExitoso()) {
                item.setEstado(EstadoItem.PAGADO);
                log.info("Saga 14.2 — medicamento {} descontado (cantidad={})", item.getIdItem(), item.getCantidad());
            } else {
                item.setEstado(EstadoItem.NO_DISPONIBLE);
                log.warn("Saga 14.2 — medicamento {} sin stock suficiente", item.getIdItem());
            }
        } catch (FeignException ex) {
            // Fallo técnico: ítem queda PENDIENTE para reintento posterior
            log.warn("Saga 14.2 — fallo técnico al descontar stock medicamento {}: {}", item.getIdItem(), ex.getMessage());
        }
    }

    private void procesarExamen(ItemProforma item, Long idPaciente) {
        try {
            laboratorioClient.autorizarExamen(
                    new ExamenAutorizadoRequestDTO(idPaciente, item.getIdEpisodioClinico(), item.getIdItem()));
            item.setEstado(EstadoItem.PAGADO);
            log.info("Saga 14.2 — examen {} autorizado", item.getIdItem());
        } catch (FeignException ex) {
            if (ex.status() >= 400 && ex.status() < 500) {
                item.setEstado(EstadoItem.NO_DISPONIBLE);
                log.warn("Saga 14.2 — examen {} no autorizable (error negocio): {}", item.getIdItem(), ex.getMessage());
            } else {
                log.warn("Saga 14.2 — fallo técnico al autorizar examen {}: {}", item.getIdItem(), ex.getMessage());
                // queda PENDIENTE
            }
        }
    }

    @Transactional
    public ComprobanteResponseDTO emitirComprobante(Long idProforma) {
        Proforma proforma = proformaRepository.findById(idProforma)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Proforma no encontrada con id: " + idProforma));

        BigDecimal total = proforma.getItems().stream()
                .filter(i -> i.getEstado() == EstadoItem.PAGADO)
                .map(ItemProforma::getPrecioCongelado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(BigDecimal.ZERO) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No hay ítems pagados en la proforma para emitir un comprobante.");
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
        comprobante = comprobanteRepository.save(comprobante);

        return toComprobanteResponse(comprobante);
    }

    private ProformaResponseDTO toResponse(Proforma p) {
        ProformaResponseDTO dto = new ProformaResponseDTO();
        dto.setId(p.getId()); dto.setIdPaciente(p.getIdPaciente());
        dto.setFechaGeneracion(p.getFechaGeneracion());
        dto.setItems(p.getItems() == null ? List.of() : p.getItems().stream().map(i -> {
            ItemProformaResponseDTO id = new ItemProformaResponseDTO();
            id.setId(i.getId()); id.setTipo(i.getTipo()); id.setIdItem(i.getIdItem());
            id.setDescripcion(i.getDescripcion()); id.setPrecioCongelado(i.getPrecioCongelado());
            id.setCantidad(i.getCantidad()); id.setEstado(i.getEstado());
            return id;
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
