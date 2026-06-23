package com.clinica.laboratorio.service;

import com.clinica.laboratorio.dto.*;
import com.clinica.laboratorio.model.Examen;
import com.clinica.laboratorio.model.ExamenAutorizado;
import com.clinica.laboratorio.repository.ExamenAutorizadoRepository;
import com.clinica.laboratorio.repository.ExamenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExamenService {

    private final ExamenRepository examenRepository;
    private final ExamenAutorizadoRepository examenAutorizadoRepository;

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

        return toAutorizadoResponse(examenAutorizadoRepository.save(autorizado));
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
