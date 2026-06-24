package com.clinica.farmacia.service;

import com.clinica.farmacia.dto.*;
import com.clinica.farmacia.model.Inventario;
import com.clinica.farmacia.model.Lote;
import com.clinica.farmacia.model.Medicamento;
import com.clinica.farmacia.repository.InventarioRepository;
import com.clinica.farmacia.repository.LoteRepository;
import com.clinica.farmacia.repository.MedicamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final LoteRepository loteRepository;
    private final InventarioRepository inventarioRepository;

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

        return toLoteResponse(lote, request.getCantidadInicial());
    }

    @Transactional
    public DescontarStockResponseDTO descontarStock(Long idMedicamento, DescontarStockRequestDTO request) {
        findById(idMedicamento);

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
            inv.setCantidadDisponible(inv.getCantidadDisponible() - descontar);
            inventarioRepository.save(inv);
            restante -= descontar;
        }

        return DescontarStockResponseDTO.exito(requerido);
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
