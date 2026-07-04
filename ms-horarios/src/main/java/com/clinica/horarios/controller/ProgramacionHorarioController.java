package com.clinica.horarios.controller;

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
            description = "Registra una franja horaria para un médico en un consultorio, en una fecha concreta. " +
                          "Valida que el consultorio no esté asignado a otro personal en esa fecha y franja, " +
                          "y que la fecha no sea anterior a hoy.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Turno creado",
                    content = @Content(schema = @Schema(implementation = ProgramacionHorarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, rango horario incorrecto o fecha pasada"),
            @ApiResponse(responseCode = "404", description = "Consultorio no encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflicto: el consultorio ya está ocupado en esa fecha y franja por otro personal")
    })
    @PostMapping
    public ResponseEntity<ProgramacionHorarioResponseDTO> crear(
            @Valid @RequestBody ProgramacionHorarioRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(programacionHorarioService.crear(request));
    }

    @Operation(summary = "Consultar turno por ID",
            description = "Retorna el detalle de un turno de programación horaria específico")
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
            description = "Actualización parcial. Solo se modifican los campos presentes en el body (no nulos). " +
                          "No se puede modificar un turno cuya fecha actual o nueva sea anterior a hoy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turno actualizado",
                    content = @Content(schema = @Schema(implementation = ProgramacionHorarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Rango horario incorrecto o fecha pasada"),
            @ApiResponse(responseCode = "404", description = "Turno o consultorio no encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflicto de horario, o el turno ya pasó")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ProgramacionHorarioResponseDTO> actualizar(
            @Parameter(description = "ID del turno", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody ProgramacionHorarioUpdateRequestDTO request) {
        return ResponseEntity.ok(programacionHorarioService.actualizar(id, request));
    }

    @Operation(summary = "Eliminar turno",
            description = "Elimina un turno. No se permite eliminar turnos cuya fecha ya pasó.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Turno eliminado"),
            @ApiResponse(responseCode = "404", description = "Turno no encontrado"),
            @ApiResponse(responseCode = "409", description = "El turno ya pasó y no puede eliminarse")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del turno", example = "1", required = true)
            @PathVariable Long id) {
        programacionHorarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener turnos de un médico",
            description = "Lista los turnos de un médico. Si se envían desde/hasta, filtra por ese rango de fechas " +
                          "(usado por la vista de calendario mensual); si no, retorna todo su historial. " +
                          "Consumido por ms-citas para calcular disponibilidad.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de turnos del médico")
    })
    @GetMapping("/personal/{idPersonal}")
    public ResponseEntity<List<ProgramacionHorarioResponseDTO>> obtenerPorPersonal(
            @Parameter(description = "ID del personal en ms-personal", example = "5", required = true)
            @PathVariable Long idPersonal,
            @Parameter(description = "Fecha de inicio del rango (inclusive)", example = "2026-07-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(description = "Fecha de fin del rango (inclusive)", example = "2026-07-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(programacionHorarioService.obtenerPorPersonal(idPersonal, desde, hasta));
    }

    @Operation(summary = "Obtener turnos de un consultorio",
            description = "Lista todos los turnos asignados a un consultorio físico, de cualquier médico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de turnos del consultorio"),
            @ApiResponse(responseCode = "404", description = "Consultorio no encontrado")
    })
    @GetMapping("/consultorio/{idConsultorio}")
    public ResponseEntity<List<ProgramacionHorarioResponseDTO>> obtenerPorConsultorio(
            @Parameter(description = "ID del consultorio físico", example = "1", required = true)
            @PathVariable Long idConsultorio) {
        return ResponseEntity.ok(programacionHorarioService.obtenerPorConsultorio(idConsultorio));
    }

    @Operation(summary = "Listar todos los turnos",
            description = "Retorna la programación horaria completa de todos los médicos y consultorios.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista completa de turnos")
    })
    @GetMapping
    public ResponseEntity<List<ProgramacionHorarioResponseDTO>> listar() {
        return ResponseEntity.ok(programacionHorarioService.listar());
    }
}
