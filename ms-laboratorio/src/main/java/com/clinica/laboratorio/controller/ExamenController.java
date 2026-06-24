package com.clinica.laboratorio.controller;

import com.clinica.laboratorio.dto.ExamenRequestDTO;
import com.clinica.laboratorio.dto.ExamenResponseDTO;
import com.clinica.laboratorio.dto.ExamenUpdateRequestDTO;
import com.clinica.laboratorio.dto.PrecioExamenResponseDTO;
import com.clinica.laboratorio.service.ExamenService;
import java.util.List;
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

@Tag(name = "Exámenes", description = "Catálogo maestro de exámenes clínicos")
@RestController
@RequestMapping("/examenes")
@RequiredArgsConstructor
public class ExamenController {

    private final ExamenService examenService;

    @Operation(summary = "Listar / buscar catálogo de exámenes",
            description = "Sin parámetros retorna todos los exámenes. Con `q` filtra por nombre o categoría (parcial).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de exámenes")
    })
    @GetMapping
    public ResponseEntity<List<ExamenResponseDTO>> listar(
            @Parameter(description = "Término de búsqueda: nombre o categoría", example = "Hemograma")
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(examenService.listarCatalogo(q));
    }

    @Operation(summary = "Registrar examen",
            description = "Agrega un examen al catálogo con su precio")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Examen registrado",
                    content = @Content(schema = @Schema(implementation = ExamenResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<ExamenResponseDTO> crear(@Valid @RequestBody ExamenRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(examenService.crear(request));
    }

    @Operation(summary = "Consultar examen por ID",
            description = "Retorna el examen sin precio. Consumido por ms-atencion-medica para que el médico seleccione al ordenar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Examen encontrado",
                    content = @Content(schema = @Schema(implementation = ExamenResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Examen no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExamenResponseDTO> obtenerCatalogo(
            @Parameter(description = "ID interno del examen", example = "12", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(examenService.obtenerCatalogo(id));
    }

    @Operation(summary = "Actualizar examen",
            description = "Actualización parcial del catálogo. El precio actualizado afecta solo a nuevas proformas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Examen actualizado",
                    content = @Content(schema = @Schema(implementation = ExamenResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Examen no encontrado")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ExamenResponseDTO> actualizar(
            @Parameter(description = "ID interno del examen", example = "12", required = true)
            @PathVariable Long id,
            @RequestBody ExamenUpdateRequestDTO request) {
        return ResponseEntity.ok(examenService.actualizar(id, request));
    }

    @Operation(summary = "Consultar precio vigente",
            description = "Retorna el precio del examen. Endpoint EXCLUSIVO para ms-caja — " +
                          "ningún otro servicio debe consumirlo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Precio encontrado",
                    content = @Content(schema = @Schema(implementation = PrecioExamenResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Examen no encontrado")
    })
    @GetMapping("/{id}/precio")
    public ResponseEntity<PrecioExamenResponseDTO> obtenerPrecio(
            @Parameter(description = "ID interno del examen", example = "12", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(examenService.obtenerPrecio(id));
    }
}
