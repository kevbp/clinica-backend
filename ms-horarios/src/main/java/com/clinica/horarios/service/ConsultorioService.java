package com.clinica.horarios.service;

import com.clinica.horarios.dto.ConsultorioRequestDTO;
import com.clinica.horarios.dto.ConsultorioResponseDTO;
import com.clinica.horarios.dto.ConsultorioUpdateRequestDTO;
import com.clinica.horarios.model.Consultorio;
import com.clinica.horarios.repository.ConsultorioRepository;
import com.clinica.horarios.repository.ProgramacionHorarioRepository;
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
    private final ProgramacionHorarioRepository programacionHorarioRepository;

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

    @Transactional
    public ConsultorioResponseDTO actualizar(Long id, ConsultorioUpdateRequestDTO request) {
        Consultorio consultorio = findById(id);
        if (request.getNumero()    != null) consultorio.setNumero(request.getNumero());
        if (request.getPiso()      != null) consultorio.setPiso(request.getPiso());
        if (request.getUbicacion() != null) consultorio.setUbicacion(request.getUbicacion());
        return toResponse(consultorioRepository.save(consultorio));
    }

    @Transactional
    public void eliminar(Long id) {
        Consultorio consultorio = findById(id);
        if (programacionHorarioRepository.existsByConsultorioId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el consultorio: tiene turnos de programación asignados.");
        }
        consultorioRepository.delete(consultorio);
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
