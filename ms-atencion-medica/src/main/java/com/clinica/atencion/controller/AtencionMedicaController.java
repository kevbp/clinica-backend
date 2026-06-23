package com.clinica.atencion.controller;

import com.clinica.atencion.dto.*;
import com.clinica.atencion.model.BorradorAtencion;
import com.clinica.atencion.service.AtencionMedicaService;
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

@Tag(name = "Atención Médica",
        description = "Estación de trabajo digital del médico (CPOE). Borrador transitorio en Redis. Sin precios.")
@RestController
@RequestMapping("/atenciones")
@RequiredArgsConstructor
public class AtencionMedicaController {

    private final AtencionMedicaService service;

    @Operation(summary = "Iniciar atención",
            description = "Abre la atención solo si la cita está en CONFIRMADA. Crea el borrador en Redis con TTL de 8h.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Borrador creado",
                    content = @Content(schema = @Schema(implementation = BorradorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "La cita no está en CONFIRMADA"),
            @ApiResponse(responseCode = "409", description = "Ya existe un borrador activo para esta cita"),
            @ApiResponse(responseCode = "502", description = "ms-citas no disponible")
    })
    @PostMapping("/iniciar")
    public ResponseEntity<BorradorResponseDTO> iniciar(
            @Valid @RequestBody IniciarAtencionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.iniciar(request));
    }

    @Operation(summary = "Autoguardado del borrador",
            description = "Reemplaza el estado completo del borrador. Usado para autoguardado incremental mientras el médico trabaja.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Borrador actualizado"),
            @ApiResponse(responseCode = "404", description = "No existe borrador para esta cita")
    })
    @PutMapping("/{idCita}/borrador")
    public ResponseEntity<BorradorResponseDTO> actualizarBorrador(
            @Parameter(description = "ID de la cita", example = "100", required = true)
            @PathVariable Long idCita,
            @RequestBody BorradorAtencion borrador) {
        return ResponseEntity.ok(service.actualizarBorrador(idCita, borrador));
    }

    @Operation(summary = "Recuperar borrador",
            description = "Recupera el borrador tras un cierre accidental del navegador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Borrador encontrado",
                    content = @Content(schema = @Schema(implementation = BorradorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "No existe borrador activo para esta cita")
    })
    @GetMapping("/{idCita}/borrador")
    public ResponseEntity<BorradorResponseDTO> obtenerBorrador(
            @Parameter(description = "ID de la cita", example = "100", required = true)
            @PathVariable Long idCita) {
        return ResponseEntity.ok(service.obtenerBorrador(idCita));
    }

    @Operation(summary = "Agregar diagnóstico CIE-10",
            description = "Agrega o reemplaza el diagnóstico del borrador. Valida formato CIE-10.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Diagnóstico agregado",
                    content = @Content(schema = @Schema(implementation = BorradorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Código CIE-10 inválido o datos faltantes"),
            @ApiResponse(responseCode = "404", description = "No existe borrador para esta cita")
    })
    @PostMapping("/{idCita}/diagnostico")
    public ResponseEntity<BorradorResponseDTO> agregarDiagnostico(
            @Parameter(description = "ID de la cita", example = "100", required = true)
            @PathVariable Long idCita,
            @Valid @RequestBody DiagnosticoRequestDTO request) {
        return ResponseEntity.ok(service.agregarDiagnostico(idCita, request));
    }

    @Operation(summary = "Agregar línea de receta",
            description = "Agrega un medicamento al borrador. Consulta antecedentes/alergias del paciente " +
                          "(ms-pacientes) y stock disponible (ms-farmacia) como advertencias al médico. " +
                          "NO descuenta stock. NO consulta precio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Línea de receta agregada con advertencias",
                    content = @Content(schema = @Schema(implementation = AgregarRecetaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "No existe borrador para esta cita"),
            @ApiResponse(responseCode = "502", description = "ms-pacientes o ms-farmacia no disponible")
    })
    @PostMapping("/{idCita}/recetas")
    public ResponseEntity<AgregarRecetaResponseDTO> agregarReceta(
            @Parameter(description = "ID de la cita", example = "100", required = true)
            @PathVariable Long idCita,
            @Valid @RequestBody AgregarRecetaRequestDTO request) {
        return ResponseEntity.ok(service.agregarReceta(idCita, request));
    }

    @Operation(summary = "Agregar línea de orden de laboratorio",
            description = "Agrega un examen al borrador. Consulta el catálogo de ms-laboratorio para " +
                          "mostrar nombre/categoría. NO consulta precio. NO crea ExamenAutorizado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Línea de orden agregada",
                    content = @Content(schema = @Schema(implementation = AgregarOrdenResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "No existe borrador o examen no encontrado en catálogo"),
            @ApiResponse(responseCode = "502", description = "ms-laboratorio no disponible")
    })
    @PostMapping("/{idCita}/ordenes-examen")
    public ResponseEntity<AgregarOrdenResponseDTO> agregarOrden(
            @Parameter(description = "ID de la cita", example = "100", required = true)
            @PathVariable Long idCita,
            @Valid @RequestBody AgregarOrdenRequestDTO request) {
        return ResponseEntity.ok(service.agregarOrden(idCita, request));
    }

    @Operation(summary = "Finalizar atención",
            description = "Flujo en orden estricto: " +
                          "(1) Marca cita ATENDIDA en ms-citas (síncrono — si falla, se aborta todo). " +
                          "(2) Publica EpisodioFinalizado a RabbitMQ para ms-historias-clinicas. " +
                          "(3) Elimina el borrador de Redis. " +
                          "El efecto en farmacia/laboratorio ocurre SOLO después, cuando el paciente paga en ms-caja.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atención finalizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Borrador sin diagnóstico — no se puede finalizar"),
            @ApiResponse(responseCode = "404", description = "No existe borrador para esta cita"),
            @ApiResponse(responseCode = "502", description = "ms-citas no disponible — finalización abortada")
    })
    @PostMapping("/{idCita}/finalizar")
    public ResponseEntity<BorradorResponseDTO> finalizar(
            @Parameter(description = "ID de la cita", example = "100", required = true)
            @PathVariable Long idCita) {
        return ResponseEntity.ok(service.finalizar(idCita));
    }
}
