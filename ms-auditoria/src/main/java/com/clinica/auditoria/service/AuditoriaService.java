package com.clinica.auditoria.service;

import com.clinica.auditoria.dto.AccionUsuarioRequestDTO;
import com.clinica.auditoria.dto.AccionUsuarioResponseDTO;
import com.clinica.auditoria.model.AccionUsuario;
import com.clinica.auditoria.repository.AccionUsuarioRepository;
import com.clinica.auditoria.util.JwtClaimsExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AccionUsuarioRepository repository;

    @Transactional
    public AccionUsuarioResponseDTO registrar(AccionUsuarioRequestDTO request, String authHeader) {
        AccionUsuario accion = new AccionUsuario();
        accion.setLogType(request.getLogType() != null ? request.getLogType() : "ACTION_LOG");
        accion.setCorrelationId(request.getCorrelationId());

        // Usuario y rol extraídos del JWT — no del body del request
        accion.setKeycloakUserId(JwtClaimsExtractor.extractUserId(authHeader));
        accion.setRol(JwtClaimsExtractor.extractRol(authHeader));

        accion.setModulo(request.getModulo());
        accion.setAccion(request.getAccion());
        accion.setEntidadTipo(request.getEntidadTipo());
        accion.setEntidadId(request.getEntidadId());
        accion.setDisparaEvento(request.getDisparaEvento());
        accion.setResultado(request.getResultado() != null ? request.getResultado() : "EXITO");
        accion.setMetadatos(request.getMetadatos());
        accion.setErrorDetalle(request.getErrorDetalle());
        // Usar el timestamp del emisor si fue enviado; de lo contrario usar el reloj local como fallback
        accion.setTimestamp(request.getTimestamp() != null
                ? Instant.ofEpochMilli(request.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now());
        accion.setDuracionMs(request.getDuracionMs());
        accion.setOrigen(request.getOrigen());
        accion.setHttpMethod(request.getHttpMethod());
        accion.setHttpPath(request.getHttpPath());
        accion.setHttpStatus(request.getHttpStatus());

        AccionUsuario saved = repository.save(accion);

        MDC.put("logType", saved.getLogType());
        MDC.put("correlationId", saved.getCorrelationId() != null ? saved.getCorrelationId() : "");
        log.info("{} usuario={} rol={} modulo={} accion={} entidad={}/{} resultado={} disparaEvento={}", saved.getLogType(),
                saved.getKeycloakUserId(), saved.getRol(), saved.getModulo(),
                saved.getAccion(), saved.getEntidadTipo(), saved.getEntidadId(),
                saved.getResultado(), saved.getDisparaEvento());
        MDC.remove("logType");
        MDC.remove("correlationId");

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AccionUsuarioResponseDTO> listarPorAccion(String accion) {
        return repository.findByAccionOrderByTimestampDesc(accion)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AccionUsuarioResponseDTO> listarPorEntidad(String tipo, String id) {
        return repository.findByEntidadTipoAndEntidadIdOrderByTimestampAsc(tipo, id)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AccionUsuarioResponseDTO> filtrar(String modulo, String accion,
                                                   String resultado,
                                                   LocalDateTime desde, LocalDateTime hasta) {
        return repository.filtrar(modulo, accion, resultado, desde, hasta)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AccionUsuarioResponseDTO> listarPorUsuario(String keycloakUserId) {
        return repository.findByKeycloakUserIdOrderByTimestampDesc(keycloakUserId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AccionUsuarioResponseDTO> listarPorCorrelationId(String correlationId) {
        return repository.findByCorrelationIdOrderByTimestampAscIdAsc(correlationId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AccionUsuarioResponseDTO> listarTodas() {
        return repository.findAll().stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .map(this::toResponse).toList();
    }

    private AccionUsuarioResponseDTO toResponse(AccionUsuario a) {
        AccionUsuarioResponseDTO dto = new AccionUsuarioResponseDTO();
        dto.setId(a.getId());
        dto.setLogType(a.getLogType());
        dto.setCorrelationId(a.getCorrelationId());
        dto.setKeycloakUserId(a.getKeycloakUserId());
        dto.setRol(a.getRol());
        dto.setModulo(a.getModulo());
        dto.setAccion(a.getAccion());
        dto.setEntidadTipo(a.getEntidadTipo());
        dto.setEntidadId(a.getEntidadId());
        dto.setDisparaEvento(a.getDisparaEvento());
        dto.setResultado(a.getResultado());
        dto.setMetadatos(a.getMetadatos());
        dto.setErrorDetalle(a.getErrorDetalle());
        dto.setTimestamp(a.getTimestamp());
        dto.setDuracionMs(a.getDuracionMs());
        dto.setOrigen(a.getOrigen());
        dto.setHttpMethod(a.getHttpMethod());
        dto.setHttpPath(a.getHttpPath());
        dto.setHttpStatus(a.getHttpStatus());
        return dto;
    }
}
