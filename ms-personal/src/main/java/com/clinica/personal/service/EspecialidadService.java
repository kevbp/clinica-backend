package com.clinica.personal.service;

import com.clinica.personal.dto.EspecialidadRequestDTO;
import com.clinica.personal.dto.EspecialidadResponseDTO;
import com.clinica.personal.model.Especialidad;
import com.clinica.personal.repository.EspecialidadRepository;
import com.clinica.personal.repository.PersonalMedicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EspecialidadService {

    private final EspecialidadRepository especialidadRepository;
    private final PersonalMedicoRepository personalMedicoRepository;

    @Transactional
    public EspecialidadResponseDTO crear(EspecialidadRequestDTO request) {
        Especialidad especialidad = new Especialidad();
        especialidad.setNombre(request.getNombre());
        especialidad.setDescripcion(request.getDescripcion());
        return toResponse(especialidadRepository.save(especialidad));
    }

    @Transactional(readOnly = true)
    public List<EspecialidadResponseDTO> listar() {
        return especialidadRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EspecialidadResponseDTO obtenerPorId(Long id) {
        return toResponse(obtenerEntidadPorId(id));
    }

    @Transactional
    public EspecialidadResponseDTO actualizar(Long id, EspecialidadRequestDTO request) {
        Especialidad especialidad = obtenerEntidadPorId(id);
        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            especialidad.setNombre(request.getNombre());
        }
        if (request.getDescripcion() != null) {
            especialidad.setDescripcion(request.getDescripcion());
        }
        return toResponse(especialidadRepository.save(especialidad));
    }

    @Transactional
    public void eliminar(Long id) {
        obtenerEntidadPorId(id);
        if (personalMedicoRepository.existsByEspecialidadId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar la especialidad porque tiene médicos asignados. Reasigne o desactive los médicos primero.");
        }
        especialidadRepository.deleteById(id);
    }

    public Especialidad obtenerEntidadPorId(Long id) {
        return especialidadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Especialidad no encontrada con id: " + id));
    }

    private EspecialidadResponseDTO toResponse(Especialidad e) {
        EspecialidadResponseDTO dto = new EspecialidadResponseDTO();
        dto.setId(e.getId());
        dto.setNombre(e.getNombre());
        dto.setDescripcion(e.getDescripcion());
        return dto;
    }
}
