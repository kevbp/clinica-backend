package com.clinica.horarios.service;

import com.clinica.horarios.dto.ConsultorioRequestDTO;
import com.clinica.horarios.dto.ConsultorioResponseDTO;
import com.clinica.horarios.model.Consultorio;
import com.clinica.horarios.repository.ConsultorioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultorioService {

    private final ConsultorioRepository consultorioRepository;

    @Transactional
    public ConsultorioResponseDTO crear(ConsultorioRequestDTO request) {
        Consultorio consultorio = new Consultorio();
        consultorio.setNumero(request.getNumero());
        consultorio.setPiso(request.getPiso());
        consultorio.setUbicacion(request.getUbicacion());
        return toResponse(consultorioRepository.save(consultorio));
    }

    @Transactional(readOnly = true)
    public List<ConsultorioResponseDTO> listar() {
        return consultorioRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConsultorioResponseDTO obtenerPorId(Long id) {
        return toResponse(findById(id));
    }

    public Consultorio findById(Long id) {
        return consultorioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Consultorio no encontrado con id: " + id));
    }

    public ConsultorioResponseDTO toResponse(Consultorio c) {
        ConsultorioResponseDTO dto = new ConsultorioResponseDTO();
        dto.setId(c.getId());
        dto.setNumero(c.getNumero());
        dto.setPiso(c.getPiso());
        dto.setUbicacion(c.getUbicacion());
        return dto;
    }
}
