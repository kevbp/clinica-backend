package com.clinica.horarios.controller;

import com.clinica.horarios.dto.ConsultorioRequestDTO;
import com.clinica.horarios.dto.ConsultorioResponseDTO;
import com.clinica.horarios.dto.ConsultorioUpdateRequestDTO;
import com.clinica.horarios.service.ConsultorioService;
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

@Tag(name = "Consultorios", description = "Gestión de consultorios físicos de la clínica")
@RestController
@RequestMapping("/consultorios")
@RequiredArgsConstructor
public class ConsultorioController {

    private final ConsultorioService consultorioService;

    @Operation(summary = "Registrar consultorio")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Consultorio registrado",
                    content = @Content(schema = @Schema(implementation = ConsultorioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<ConsultorioResponseDTO> crear(
            @Valid @RequestBody ConsultorioRequestDTO request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultorioService.crear(request, authHeader));
    }

    @Operation(summary = "Listar consultorios")
    @GetMapping
    public ResponseEntity<List<ConsultorioResponseDTO>> listar() {
        return ResponseEntity.ok(consultorioService.listar());
    }

    @Operation(summary = "Consultar consultorio por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consultorio encontrado",
                    content = @Content(schema = @Schema(implementation = ConsultorioResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Consultorio no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConsultorioResponseDTO> obtenerPorId(
            @Parameter(description = "ID interno del consultorio", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(consultorioService.obtenerPorId(id));
    }

    @Operation(summary = "Actualizar consultorio")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consultorio actualizado",
                    content = @Content(schema = @Schema(implementation = ConsultorioResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Consultorio no encontrado")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ConsultorioResponseDTO> actualizar(
            @Parameter(description = "ID interno del consultorio", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody ConsultorioUpdateRequestDTO request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(consultorioService.actualizar(id, request, authHeader));
    }

    @Operation(summary = "Eliminar consultorio",
            description = "Falla con 409 si tiene turnos de programación asignados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Consultorio eliminado"),
            @ApiResponse(responseCode = "404", description = "Consultorio no encontrado"),
            @ApiResponse(responseCode = "409", description = "El consultorio tiene turnos de programación asignados")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID interno del consultorio", example = "1", required = true)
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        consultorioService.eliminar(id, authHeader);
        return ResponseEntity.noContent().build();
    }
}
