package com.clinica.laboratorio.service;

import com.clinica.laboratorio.client.AuditoriaClient;
import com.clinica.laboratorio.dto.*;
import com.clinica.laboratorio.model.Examen;
import com.clinica.laboratorio.model.ExamenAutorizado;
import com.clinica.laboratorio.repository.ExamenAutorizadoRepository;
import com.clinica.laboratorio.repository.ExamenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
public class ExamenService {

    private static final String MODULO = "LABORATORIO";

    private final ExamenRepository examenRepository;
    private final ExamenAutorizadoRepository examenAutorizadoRepository;
    private final AuditoriaClient auditoriaClient;

    @Transactional
    public ExamenResponseDTO crear(ExamenRequestDTO request) {
        Examen examen = new Examen();
        examen.setNombre(request.getNombre());
        examen.setCategoria(request.getCategoria());
        examen.setDescripcion(request.getDescripcion());
        examen.setPrecio(request.getPrecio());
        return toCatalogoResponse(examenRepository.save(examen));
    }

    @Transactional(readOnly = true)
    public List<ExamenResponseDTO> listarCatalogo(String q) {
        List<Examen> result = (q == null || q.isBlank())
                ? examenRepository.findAll()
                : examenRepository.findByNombreContainingIgnoreCaseOrCategoriaContainingIgnoreCase(q, q);
        return result.stream().map(this::toCatalogoResponse).toList();
    }

    @Transactional
    public ExamenResponseDTO actualizar(Long id, ExamenUpdateRequestDTO request) {
        Examen e = findById(id);
        if (request.getNombre()      != null) e.setNombre(request.getNombre());
        if (request.getCategoria()   != null) e.setCategoria(request.getCategoria());
        if (request.getDescripcion() != null) e.setDescripcion(request.getDescripcion());
        if (request.getPrecio()      != null) e.setPrecio(request.getPrecio());
        return toCatalogoResponse(examenRepository.save(e));
    }

    @Transactional(readOnly = true)
    public List<ExamenAutorizadoResponseDTO> listarAutorizadosPorPaciente(Long idPaciente) {
        return examenAutorizadoRepository.findByIdPaciente(idPaciente).stream()
                .map(this::toAutorizadoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExamenResponseDTO obtenerCatalogo(Long id) {
        return toCatalogoResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public PrecioExamenResponseDTO obtenerPrecio(Long id) {
        Examen examen = findById(id);
        return new PrecioExamenResponseDTO(examen.getId(), examen.getPrecio());
    }

    @Transactional
    public ExamenAutorizadoResponseDTO autorizar(ExamenAutorizadoRequestDTO request) {
        Examen examen = findById(request.getIdExamen());

        ExamenAutorizado autorizado = new ExamenAutorizado();
        autorizado.setIdPaciente(request.getIdPaciente());
        autorizado.setIdEpisodioClinico(request.getIdEpisodioClinico());
        autorizado.setExamen(examen);
        autorizado.setFechaAutorizacion(LocalDateTime.now());

        ExamenAutorizado saved = examenAutorizadoRepository.save(autorizado);
        auditarAsync("AUTORIZAR_EXAMEN", "ExamenAutorizado", String.valueOf(saved.getId()),
                "EXITO", null,
                "{\"idPaciente\":" + request.getIdPaciente() + ",\"idExamen\":" + request.getIdExamen() + "}");
        return toAutorizadoResponse(saved);
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

    private Examen findById(Long id) {
        return examenRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Examen no encontrado con id: " + id));
    }

    private ExamenResponseDTO toCatalogoResponse(Examen e) {
        ExamenResponseDTO dto = new ExamenResponseDTO();
        dto.setId(e.getId());
        dto.setNombre(e.getNombre());
        dto.setCategoria(e.getCategoria());
        dto.setDescripcion(e.getDescripcion());
        dto.setPrecio(e.getPrecio());
        return dto;
    }

    private ExamenAutorizadoResponseDTO toAutorizadoResponse(ExamenAutorizado a) {
        ExamenAutorizadoResponseDTO dto = new ExamenAutorizadoResponseDTO();
        dto.setId(a.getId());
        dto.setIdPaciente(a.getIdPaciente());
        dto.setIdEpisodioClinico(a.getIdEpisodioClinico());
        dto.setIdExamen(a.getExamen().getId());
        dto.setNombreExamen(a.getExamen().getNombre());
        dto.setFechaAutorizacion(a.getFechaAutorizacion());
        return dto;
    }
}
