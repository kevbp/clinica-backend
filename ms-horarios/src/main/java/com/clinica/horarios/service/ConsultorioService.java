package com.clinica.horarios.service;

import com.clinica.horarios.client.AuditoriaClient;
import com.clinica.horarios.dto.AccionAuditoriaDTO;
import com.clinica.horarios.dto.ConsultorioRequestDTO;
import com.clinica.horarios.dto.ConsultorioResponseDTO;
import com.clinica.horarios.dto.ConsultorioUpdateRequestDTO;
import com.clinica.horarios.model.Consultorio;
import com.clinica.horarios.repository.ConsultorioRepository;
import com.clinica.horarios.repository.ProgramacionHorarioRepository;
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
public class ConsultorioService {

    private static final String MODULO = "HORARIOS";

    private final ConsultorioRepository consultorioRepository;
    private final ProgramacionHorarioRepository programacionHorarioRepository;
    private final AuditoriaClient auditoriaClient;

    @Transactional
    public ConsultorioResponseDTO crear(ConsultorioRequestDTO request, String authHeader) {
        Consultorio consultorio = new Consultorio();
        consultorio.setNumero(request.getNumero());
        consultorio.setPiso(request.getPiso());
        consultorio.setUbicacion(request.getUbicacion());
        Consultorio saved = consultorioRepository.save(consultorio);

        auditarAsync("CREAR_CONSULTORIO", "Consultorio", String.valueOf(saved.getId()),
                "EXITO", authHeader,
                "{\"numero\":\"" + saved.getNumero() + "\",\"piso\":" + saved.getPiso() + "}");

        return toResponse(saved);
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
    public ConsultorioResponseDTO actualizar(Long id, ConsultorioUpdateRequestDTO request, String authHeader) {
        Consultorio consultorio = findById(id);
        if (request.getNumero()    != null) consultorio.setNumero(request.getNumero());
        if (request.getPiso()      != null) consultorio.setPiso(request.getPiso());
        if (request.getUbicacion() != null) consultorio.setUbicacion(request.getUbicacion());
        Consultorio saved = consultorioRepository.save(consultorio);

        auditarAsync("ACTUALIZAR_CONSULTORIO", "Consultorio", String.valueOf(id),
                "EXITO", authHeader, null);

        return toResponse(saved);
    }

    @Transactional
    public void eliminar(Long id, String authHeader) {
        Consultorio consultorio = findById(id);
        if (programacionHorarioRepository.existsByConsultorioId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el consultorio: tiene turnos de programación asignados.");
        }
        consultorioRepository.delete(consultorio);

        auditarAsync("ELIMINAR_CONSULTORIO", "Consultorio", String.valueOf(id),
                "EXITO", authHeader, null);
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

    // ── Mapeo (public para que ProgramacionHorarioService pueda reusar) ──────

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
