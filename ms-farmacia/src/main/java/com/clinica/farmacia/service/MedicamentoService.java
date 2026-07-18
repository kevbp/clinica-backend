package com.clinica.farmacia.service;

import com.clinica.farmacia.client.AuditoriaClient;
import com.clinica.farmacia.dto.*;
import com.clinica.farmacia.model.Inventario;
import com.clinica.farmacia.model.Lote;
import com.clinica.farmacia.model.Medicamento;
import com.clinica.farmacia.model.MovimientoInventario;
import com.clinica.farmacia.repository.InventarioRepository;
import com.clinica.farmacia.repository.LoteRepository;
import com.clinica.farmacia.repository.MedicamentoRepository;
import com.clinica.farmacia.repository.MovimientoInventarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicamentoService {

    private static final String MODULO = "FARMACIA";

    private final MedicamentoRepository medicamentoRepository;
    private final LoteRepository loteRepository;
    private final InventarioRepository inventarioRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final AuditoriaClient auditoriaClient;

    @Transactional
    public MedicamentoResponseDTO crear(MedicamentoRequestDTO request) {
        Medicamento m = new Medicamento();
        m.setNombre(request.getNombre());
        m.setPrincipioActivo(request.getPrincipioActivo());
        m.setPresentacion(request.getPresentacion());
        m.setPrecio(request.getPrecio());
        return toCatalogoResponse(medicamentoRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<MedicamentoResponseDTO> listarCatalogo(String q) {
        List<Medicamento> result = (q == null || q.isBlank())
                ? medicamentoRepository.findAll()
                : medicamentoRepository.findByNombreContainingIgnoreCaseOrPrincipioActivoContainingIgnoreCase(q, q);
        return result.stream().map(this::toCatalogoResponse).toList();
    }

    @Transactional
    public MedicamentoResponseDTO actualizar(Long id, MedicamentoUpdateRequestDTO request) {
        Medicamento m = findById(id);
        if (request.getNombre()          != null) m.setNombre(request.getNombre());
        if (request.getPrincipioActivo() != null) m.setPrincipioActivo(request.getPrincipioActivo());
        if (request.getPresentacion()    != null) m.setPresentacion(request.getPresentacion());
        if (request.getPrecio()          != null) m.setPrecio(request.getPrecio());
        return toCatalogoResponse(medicamentoRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<LoteResponseDTO> listarLotes(Long idMedicamento) {
        findById(idMedicamento);
        List<Lote> lotes = loteRepository.findByMedicamentoId(idMedicamento);
        Map<Long, Integer> stockPorLote = inventarioRepository.findByMedicamentoId(idMedicamento).stream()
                .collect(java.util.stream.Collectors.toMap(
                        inv -> inv.getLote().getId(),
                        Inventario::getCantidadDisponible,
                        Integer::sum));
        return lotes.stream()
                .map(l -> toLoteResponse(l, stockPorLote.getOrDefault(l.getId(), 0)))
                .toList();
    }

    @Transactional(readOnly = true)
    public MedicamentoResponseDTO obtenerCatalogo(Long id) {
        return toCatalogoResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public PrecioResponseDTO obtenerPrecio(Long id) {
        Medicamento m = findById(id);
        return new PrecioResponseDTO(m.getId(), m.getPrecio());
    }

    @Transactional(readOnly = true)
    public DisponibilidadResponseDTO obtenerDisponibilidad(Long id) {
        findById(id);
        Integer total = inventarioRepository.sumStockVigente(id, LocalDate.now());
        return new DisponibilidadResponseDTO(id, total != null ? total : 0);
    }

    @Transactional
    public LoteResponseDTO agregarLote(Long idMedicamento, LoteRequestDTO request) {
        Medicamento medicamento = findById(idMedicamento);

        Lote lote = new Lote();
        lote.setMedicamento(medicamento);
        lote.setNumeroLote(request.getNumeroLote());
        lote.setFechaVencimiento(request.getFechaVencimiento());
        lote = loteRepository.save(lote);

        Inventario inventario = new Inventario();
        inventario.setLote(lote);
        inventario.setCantidadDisponible(request.getCantidadInicial());
        inventarioRepository.save(inventario);

        registrarMovimiento(medicamento, lote, MovimientoInventario.Tipo.ENTRADA,
                MovimientoInventario.Motivo.LOTE_REGISTRADO,
                request.getCantidadInicial(), 0, request.getCantidadInicial(), null);

        return toLoteResponse(lote, request.getCantidadInicial());
    }

    @Transactional
    public LoteResponseDTO actualizarLote(Long idMedicamento, Long idLote, LoteUpdateRequestDTO request) {
        findById(idMedicamento);
        Lote lote = loteRepository.findById(idLote)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lote no encontrado: " + idLote));
        if (!lote.getMedicamento().getId().equals(idMedicamento)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El lote no pertenece al medicamento indicado");
        }
        if (request.getNumeroLote() != null) lote.setNumeroLote(request.getNumeroLote());
        if (request.getFechaVencimiento() != null) lote.setFechaVencimiento(request.getFechaVencimiento());
        lote = loteRepository.save(lote);

        Inventario inv = inventarioRepository.findByLoteId(idLote)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventario no encontrado para lote: " + idLote));
        if (request.getCantidadDisponible() != null) {
            int anterior = inv.getCantidadDisponible();
            inv.setCantidadDisponible(request.getCantidadDisponible());
            inventarioRepository.save(inv);
            Medicamento medicamento = lote.getMedicamento();
            registrarMovimiento(medicamento, lote, MovimientoInventario.Tipo.AJUSTE,
                    MovimientoInventario.Motivo.AJUSTE_MANUAL,
                    Math.abs(request.getCantidadDisponible() - anterior), anterior, request.getCantidadDisponible(), null);
        }
        return toLoteResponse(lote, inv.getCantidadDisponible());
    }

    @Transactional
    public DescontarStockResponseDTO descontarStock(Long idMedicamento, DescontarStockRequestDTO request) {
        Medicamento medicamento = findById(idMedicamento);

        int requerido = request.getCantidad();
        Integer disponible = inventarioRepository.sumStockVigente(idMedicamento, LocalDate.now());
        int totalDisponible = disponible != null ? disponible : 0;

        if (totalDisponible < requerido) {
            return DescontarStockResponseDTO.fallo(requerido, totalDisponible);
        }

        // FEFO: descuenta del lote con vencimiento más próximo primero
        List<Inventario> lotesFEFO = inventarioRepository.findLotesVigentesFEFO(idMedicamento, LocalDate.now());
        int restante = requerido;
        for (Inventario inv : lotesFEFO) {
            if (restante == 0) break;
            int descontar = Math.min(restante, inv.getCantidadDisponible());
            int anterior = inv.getCantidadDisponible();
            inv.setCantidadDisponible(anterior - descontar);
            inventarioRepository.save(inv);
            registrarMovimiento(medicamento, inv.getLote(), MovimientoInventario.Tipo.SALIDA,
                    MovimientoInventario.Motivo.PAGO_PROFORMA,
                    descontar, anterior, inv.getCantidadDisponible(), request.getReferencia());
            restante -= descontar;
        }

        auditarAsync("DESCONTAR_STOCK", "Medicamento", String.valueOf(idMedicamento),
                "EXITO", null, "{\"cantidad\":" + requerido + ",\"referencia\":\"" + request.getReferencia() + "\"}");
        return DescontarStockResponseDTO.exito(requerido);
    }

    private void auditarAsync(String accion, String entidadTipo, String entidadId,
                               String resultado, String disparaEvento, String metadatos) {
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
                                .build(),
                        null);
            } catch (Exception e) {
                log.warn("ACTION_LOG no registrado [{}/{}]: {}", accion, entidadId, e.getMessage());
            }
        });
    }

    @Transactional(readOnly = true)
    public List<MedicamentoConStockResponseDTO> listarConStock(String q) {
        List<Medicamento> meds = (q == null || q.isBlank())
                ? medicamentoRepository.findAll()
                : medicamentoRepository.findByNombreContainingIgnoreCaseOrPrincipioActivoContainingIgnoreCase(q, q);
        return meds.stream().map(m -> toConStockResponse(m, listarLotes(m.getId()))).toList();
    }

    @Transactional(readOnly = true)
    public List<MedicamentoConStockResponseDTO> listarStockBajo(int umbral) {
        LocalDate hoy = LocalDate.now();
        // Evalúa el stock vigente de cada medicamento del catálogo y filtra los que
        // están por debajo del umbral. Incluye medicamentos sin ningún lote (stock = 0).
        return medicamentoRepository.findAll().stream()
                .filter(m -> {
                    Integer stock = inventarioRepository.sumStockVigente(m.getId(), hoy);
                    return (stock == null ? 0 : stock) <= umbral;
                })
                .map(m -> toConStockResponse(m, listarLotes(m.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LoteResponseDTO> listarProximosAVencer(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);
        return inventarioRepository.findLotesProximosAVencer(hoy, limite).stream()
                .map(inv -> toLoteResponse(inv.getLote(), inv.getCantidadDisponible()))
                .toList();
    }

    private MedicamentoConStockResponseDTO toConStockResponse(Medicamento m, List<LoteResponseDTO> lotes) {
        MedicamentoConStockResponseDTO dto = new MedicamentoConStockResponseDTO();
        dto.setId(m.getId());
        dto.setNombre(m.getNombre());
        dto.setPrincipioActivo(m.getPrincipioActivo());
        dto.setPresentacion(m.getPresentacion());
        dto.setPrecio(m.getPrecio());
        dto.setStockTotal(lotes.stream().mapToInt(LoteResponseDTO::getCantidadDisponible).sum());
        dto.setLotes(lotes);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<KardexResponseDTO> listarKardex(Long idMedicamento, LocalDate desde, LocalDate hasta) {
        findById(idMedicamento);
        List<MovimientoInventario> movimientos;
        if (desde != null && hasta != null) {
            movimientos = movimientoRepository.findByIdMedicamentoAndFechaBetweenOrderByFechaDesc(
                    idMedicamento,
                    desde.atStartOfDay(),
                    hasta.plusDays(1).atStartOfDay());
        } else {
            movimientos = movimientoRepository.findByIdMedicamentoOrderByFechaDesc(idMedicamento);
        }
        return movimientos.stream().map(this::toKardexResponse).toList();
    }

    private void registrarMovimiento(Medicamento medicamento, Lote lote, MovimientoInventario.Tipo tipo,
                                     MovimientoInventario.Motivo motivo, int cantidad,
                                     int saldoAnterior, int saldoPosterior, String referencia) {
        MovimientoInventario m = new MovimientoInventario();
        m.setIdMedicamento(medicamento.getId());
        m.setNombreMedicamento(medicamento.getNombre());
        m.setIdLote(lote.getId());
        m.setNumeroLote(lote.getNumeroLote());
        m.setTipo(tipo);
        m.setMotivo(motivo);
        m.setCantidad(cantidad);
        m.setSaldoAnterior(saldoAnterior);
        m.setSaldoPosterior(saldoPosterior);
        m.setReferencia(referencia);
        m.setFecha(LocalDateTime.now());
        movimientoRepository.save(m);
    }

    private KardexResponseDTO toKardexResponse(MovimientoInventario m) {
        KardexResponseDTO dto = new KardexResponseDTO();
        dto.setId(m.getId());
        dto.setIdMedicamento(m.getIdMedicamento());
        dto.setNombreMedicamento(m.getNombreMedicamento());
        dto.setIdLote(m.getIdLote());
        dto.setNumeroLote(m.getNumeroLote());
        dto.setTipo(m.getTipo());
        dto.setMotivo(m.getMotivo());
        dto.setCantidad(m.getCantidad());
        dto.setSaldoAnterior(m.getSaldoAnterior());
        dto.setSaldoPosterior(m.getSaldoPosterior());
        dto.setReferencia(m.getReferencia());
        dto.setFecha(m.getFecha());
        return dto;
    }

    private Medicamento findById(Long id) {
        return medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Medicamento no encontrado con id: " + id));
    }

    private MedicamentoResponseDTO toCatalogoResponse(Medicamento m) {
        MedicamentoResponseDTO dto = new MedicamentoResponseDTO();
        dto.setId(m.getId());
        dto.setNombre(m.getNombre());
        dto.setPrincipioActivo(m.getPrincipioActivo());
        dto.setPresentacion(m.getPresentacion());
        dto.setPrecio(m.getPrecio());
        return dto;
    }

    private LoteResponseDTO toLoteResponse(Lote lote, Integer cantidad) {
        LoteResponseDTO dto = new LoteResponseDTO();
        dto.setId(lote.getId());
        dto.setIdMedicamento(lote.getMedicamento().getId());
        dto.setNumeroLote(lote.getNumeroLote());
        dto.setFechaVencimiento(lote.getFechaVencimiento());
        dto.setCantidadDisponible(cantidad);
        return dto;
    }
}
