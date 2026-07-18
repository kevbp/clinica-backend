package com.clinica.personal.service;

import com.clinica.personal.client.AuditoriaClient;
import com.clinica.personal.dto.AccionAuditoriaDTO;
import com.clinica.personal.dto.EspecialidadRequestDTO;
import com.clinica.personal.dto.EspecialidadResponseDTO;
import com.clinica.personal.model.Especialidad;
import com.clinica.personal.repository.EspecialidadRepository;
import com.clinica.personal.repository.PersonalMedicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EspecialidadService {

    private static final String MODULO = "PERSONAL";

    private final EspecialidadRepository especialidadRepository;
    private final PersonalMedicoRepository personalMedicoRepository;
    private final AuditoriaClient auditoriaClient;

    @Transactional
    public EspecialidadResponseDTO crear(EspecialidadRequestDTO request, String authHeader) {
        Especialidad especialidad = new Especialidad();
        especialidad.setNombre(request.getNombre());
        especialidad.setDescripcion(request.getDescripcion());
        Especialidad saved = especialidadRepository.save(especialidad);

        auditarAsync("CREAR_ESPECIALIDAD", "Especialidad", String.valueOf(saved.getId()),
                "EXITO", authHeader, null);

        return toResponse(saved);
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
    public EspecialidadResponseDTO actualizar(Long id, EspecialidadRequestDTO request, String authHeader) {
        Especialidad especialidad = obtenerEntidadPorId(id);
        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            especialidad.setNombre(request.getNombre());
        }
        if (request.getDescripcion() != null) {
            especialidad.setDescripcion(request.getDescripcion());
        }
        Especialidad saved = especialidadRepository.save(especialidad);

        auditarAsync("ACTUALIZAR_ESPECIALIDAD", "Especialidad", String.valueOf(id),
                "EXITO", authHeader, null);

        return toResponse(saved);
    }

    @Transactional
    public void eliminar(Long id, String authHeader) {
        obtenerEntidadPorId(id);
        if (personalMedicoRepository.existsByEspecialidadId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar la especialidad porque tiene médicos asignados. Reasigne o desactive los médicos primero.");
        }
        especialidadRepository.deleteById(id);

        auditarAsync("ELIMINAR_ESPECIALIDAD", "Especialidad", String.valueOf(id),
                "EXITO", authHeader, null);
    }

    public Especialidad obtenerEntidadPorId(Long id) {
        return especialidadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Especialidad no encontrada con id: " + id));
    }

    // ── Auditoría ────────────────────────────────────────────────────────────

    private void auditarAsync(String accion, String entidadTipo, String entidadId,
                               String resultado, String authHeader, String metadatos) {
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
                                .metadatos(metadatos)
                                .build(),
                        authHeader);
            } catch (Exception e) {
                log.warn("ACTION_LOG no registrado [{}/{}]: {}", accion, entidadId, e.getMessage());
            }
        });
    }

    // ── Mapeo ────────────────────────────────────────────────────────────────

    private EspecialidadResponseDTO toResponse(Especialidad e) {
        EspecialidadResponseDTO dto = new EspecialidadResponseDTO();
        dto.setId(e.getId());
        dto.setNombre(e.getNombre());
        dto.setDescripcion(e.getDescripcion());
        return dto;
    }
}
