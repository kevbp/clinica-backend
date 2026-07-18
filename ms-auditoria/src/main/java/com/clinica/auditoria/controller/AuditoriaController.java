package com.clinica.auditoria.controller;

import com.clinica.auditoria.dto.AccionUsuarioRequestDTO;
import com.clinica.auditoria.dto.AccionUsuarioResponseDTO;
import com.clinica.auditoria.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;

@Tag(name = "Auditoría")
@RestController
@RequestMapping("/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @Operation(summary = "Registrar acción (llamado desde cada microservicio de dominio)")
    @PostMapping("/acciones")
    @ResponseStatus(HttpStatus.CREATED)
    public AccionUsuarioResponseDTO registrar(
            @Valid @RequestBody AccionUsuarioRequestDTO request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return auditoriaService.registrar(request, authHeader);
    }

    @Operation(summary = "Listar acciones de un usuario")
    @GetMapping("/acciones/usuario/{keycloakUserId}")
    public List<AccionUsuarioResponseDTO> porUsuario(@PathVariable String keycloakUserId) {
        return auditoriaService.listarPorUsuario(keycloakUserId);
    }

    @Operation(summary = "Listar todas las acciones (panel de auditoría)")
    @GetMapping("/acciones")
    public List<AccionUsuarioResponseDTO> todas() {
        return auditoriaService.listarTodas();
    }

    @Operation(summary = "Traza completa de un flujo por correlation ID")
    @GetMapping("/traza/{correlationId}")
    public ResponseEntity<?> traza(@PathVariable String correlationId) {
        List<AccionUsuarioResponseDTO> acciones = auditoriaService.listarPorCorrelationId(correlationId);
        if (acciones.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
            "correlationId", correlationId,
            "totalAcciones", acciones.size(),
            "historia", acciones
        ));
    }

    @Operation(summary = "Historia completa de una entidad de negocio (ej: CitaMedica/42)")
    @GetMapping("/entidad/{tipo}/{id}")
    public ResponseEntity<?> porEntidad(@PathVariable String tipo, @PathVariable String id) {
        List<AccionUsuarioResponseDTO> eventos = auditoriaService.listarPorEntidad(tipo, id);
        if (eventos.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of(
            "entidadTipo",  tipo,
            "entidadId",    id,
            "totalEventos", eventos.size(),
            "historia",     eventos
        ));
    }

    @Operation(summary = "Mensajes que cayeron en Dead Letter Queue (equivale a GET /esb/dlq del ESB de clase)")
    @GetMapping("/dlq")
    public ResponseEntity<?> listarDlq() {
        List<AccionUsuarioResponseDTO> mensajes = auditoriaService.listarPorAccion("MSG_EN_DLQ");
        return ResponseEntity.ok(Map.of(
            "totalEnDlq",    mensajes.size(),
            "mensajes",      mensajes,
            "reintento",     "POST /notificaciones/dlq/reintentar?cantidad=N",
            "accion",        mensajes.isEmpty() ? "DLQ vacia" : "Usar endpoint de reintento para re-encolar"
        ));
    }

    @Operation(summary = "Filtros combinados para panel operativo")
    @GetMapping("/filtrar")
    public List<AccionUsuarioResponseDTO> filtrar(
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String resultado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return auditoriaService.filtrar(modulo, accion, resultado, desde, hasta);
    }
}
