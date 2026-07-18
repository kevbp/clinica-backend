package com.clinica.horarios.controller;

import com.clinica.horarios.dto.ProgramacionHorarioBatchRequestDTO;
import com.clinica.horarios.dto.ProgramacionHorarioBatchResponseDTO;
import com.clinica.horarios.dto.ProgramacionHorarioRequestDTO;
import com.clinica.horarios.dto.ProgramacionHorarioResponseDTO;
import com.clinica.horarios.dto.ProgramacionHorarioUpdateRequestDTO;
import com.clinica.horarios.service.ProgramacionHorarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Programación de Horarios",
        description = "Turnos en fechas concretas de los que ms-citas deriva los bloques de cita disponibles")
@RestController
@RequestMapping("/programacion-horarios")
@RequiredArgsConstructor
public class ProgramacionHorarioController {

    private final ProgramacionHorarioService programacionHorarioService;

    @Operation(summary = "Crear turno",
            description = "Registra una franja horaria para un médico en un consultorio en una fecha concreta.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Turno creado",
                    content = @Content(schema = @Schema(implementation = ProgramacionHorarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, rango horario incorrecto o fecha pasada"),
            @ApiResponse(responseCode = "404", description = "Consultorio no encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflicto: el consultorio ya está ocupado en esa franja")
    })
    @PostMapping
    public ResponseEntity<ProgramacionHorarioResponseDTO> crear(
            @Valid @RequestBody ProgramacionHorarioRequestDTO request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(programacionHorarioService.crear(request, authHeader));
    }

    @Operation(summary = "Crear turnos en lote",
            description = "Crea turnos para múltiples fechas en una sola transacción. Si alguna fecha tiene conflicto, ningún turno se crea.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Todos los turnos creados",
                    content = @Content(schema = @Schema(implementation = ProgramacionHorarioBatchResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Rango horario incorrecto o alguna fecha es pasada"),
            @ApiResponse(responseCode = "404", description = "Consultorio no encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflicto en alguna de las fechas — ningún turno fue creado")
    })
    @PostMapping("/batch")
    public ResponseEntity<ProgramacionHorarioBatchResponseDTO> crearBatch(
            @Valid @RequestBody ProgramacionHorarioBatchRequestDTO request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(programacionHorarioService.crearBatch(request, authHeader));
    }

    @Operation(summary = "Consultar turno por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turno encontrado",
                    content = @Content(schema = @Schema(implementation = ProgramacionHorarioResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Turno no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProgramacionHorarioResponseDTO> obtenerPorId(
            @Parameter(description = "ID del turno", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(programacionHorarioService.obtenerPorId(id));
    }

    @Operation(summary = "Actualizar turno",
            description = "No se puede modificar un turno cuya fecha ya pasó.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turno actualizado",
                    content = @Content(schema = @Schema(implementation = ProgramacionHorarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Rango horario incorrecto o fecha pasada"),
            @ApiResponse(responseCode = "404", description = "Turno o consultorio no encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflicto de horario o turno ya pasó")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ProgramacionHorarioResponseDTO> actualizar(
            @Parameter(description = "ID del turno", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody ProgramacionHorarioUpdateRequestDTO request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(programacionHorarioService.actualizar(id, request, authHeader));
    }

    @Operation(summary = "Eliminar turno",
            description = "No se permite eliminar turnos cuya fecha ya pasó.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Turno eliminado"),
            @ApiResponse(responseCode = "404", description = "Turno no encontrado"),
            @ApiResponse(responseCode = "409", description = "El turno ya pasó y no puede eliminarse")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del turno", example = "1", required = true)
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        programacionHorarioService.eliminar(id, authHeader);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener turnos de un médico",
            description = "Consumido por ms-citas para calcular disponibilidad.")
    @GetMapping("/personal/{idPersonal}")
    public ResponseEntity<List<ProgramacionHorarioResponseDTO>> obtenerPorPersonal(
            @Parameter(description = "ID del personal en ms-personal", example = "5", required = true)
            @PathVariable Long idPersonal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(programacionHorarioService.obtenerPorPersonal(idPersonal, desde, hasta));
    }

    @Operation(summary = "Obtener turnos de un consultorio")
    @GetMapping("/consultorio/{idConsultorio}")
    public ResponseEntity<List<ProgramacionHorarioResponseDTO>> obtenerPorConsultorio(
            @Parameter(description = "ID del consultorio físico", example = "1", required = true)
            @PathVariable Long idConsultorio) {
        return ResponseEntity.ok(programacionHorarioService.obtenerPorConsultorio(idConsultorio));
    }

    @Operation(summary = "Listar todos los turnos")
    @GetMapping
    public ResponseEntity<List<ProgramacionHorarioResponseDTO>> listar() {
        return ResponseEntity.ok(programacionHorarioService.listar());
    }
}
