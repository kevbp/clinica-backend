package com.clinica.horarios.controller;

import com.clinica.horarios.dto.ConsultorioRequestDTO;
import com.clinica.horarios.dto.ConsultorioResponseDTO;
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

@Tag(name = "Consultorios", description = "Gestión de consultorios físicos de la clínica")
@RestController
@RequestMapping("/consultorios")
@RequiredArgsConstructor
public class ConsultorioController {

    private final ConsultorioService consultorioService;

    @Operation(summary = "Registrar consultorio",
            description = "Agrega un nuevo consultorio físico al sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Consultorio registrado",
                    content = @Content(schema = @Schema(implementation = ConsultorioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<ConsultorioResponseDTO> crear(
            @Valid @RequestBody ConsultorioRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultorioService.crear(request));
    }

    @Operation(summary = "Consultar consultorio por ID",
            description = "Retorna los datos de un consultorio físico")
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
}
