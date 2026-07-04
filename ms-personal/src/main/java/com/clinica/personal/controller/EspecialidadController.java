package com.clinica.personal.controller;

import com.clinica.personal.dto.EspecialidadRequestDTO;
import com.clinica.personal.dto.EspecialidadResponseDTO;
import com.clinica.personal.service.EspecialidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Especialidades", description = "Catálogo de especialidades médicas")
@RestController
@RequestMapping("/especialidades")
@RequiredArgsConstructor
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    @Operation(summary = "Crear especialidad", description = "Agrega una nueva especialidad al catálogo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Especialidad creada",
                    content = @Content(schema = @Schema(implementation = EspecialidadResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<EspecialidadResponseDTO> crear(@Valid @RequestBody EspecialidadRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(especialidadService.crear(request));
    }

    @Operation(summary = "Listar especialidades", description = "Retorna todas las especialidades del catálogo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de especialidades")
    })
    @GetMapping
    public ResponseEntity<List<EspecialidadResponseDTO>> listar() {
        return ResponseEntity.ok(especialidadService.listar());
    }

    @Operation(summary = "Consultar especialidad por ID", description = "Retorna el detalle de una especialidad")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Especialidad encontrada",
                    content = @Content(schema = @Schema(implementation = EspecialidadResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Especialidad no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadResponseDTO> obtenerPorId(
            @Parameter(description = "ID de la especialidad", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(especialidadService.obtenerPorId(id));
    }

    @Operation(summary = "Actualizar especialidad", description = "Modifica el nombre o descripción de una especialidad")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Especialidad actualizada",
                    content = @Content(schema = @Schema(implementation = EspecialidadResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Especialidad no encontrada")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<EspecialidadResponseDTO> actualizar(
            @Parameter(description = "ID de la especialidad", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody EspecialidadRequestDTO request) {
        return ResponseEntity.ok(especialidadService.actualizar(id, request));
    }

    @Operation(summary = "Eliminar especialidad",
            description = "Elimina una especialidad del catálogo. Falla con 409 si hay médicos asignados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Especialidad eliminada"),
            @ApiResponse(responseCode = "404", description = "Especialidad no encontrada"),
            @ApiResponse(responseCode = "409", description = "Especialidad en uso — tiene médicos asignados")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la especialidad", example = "1", required = true)
            @PathVariable Long id) {
        especialidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
