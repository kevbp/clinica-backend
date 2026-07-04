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
        description = "Estación de trabajo CPOE del médico — flujo SOAP. Borrador transitorio en Redis. Sin precios.")
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
            description = "Reemplaza el estado completo del borrador. Usado para autoguardado incremental.")
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

    @Operation(summary = "Actualizar anamnesis (S+O del SOAP)",
            description = "Guarda el motivo de consulta y los signos vitales en el borrador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anamnesis guardada"),
            @ApiResponse(responseCode = "404", description = "No existe borrador para esta cita")
    })
    @PatchMapping("/{idCita}/anamnesis")
    public ResponseEntity<BorradorResponseDTO> actualizarAnamnesis(
            @Parameter(description = "ID de la cita", example = "100", required = true)
            @PathVariable Long idCita,
            @RequestBody ActualizarAnamnesisRequestDTO request) {
        return ResponseEntity.ok(service.actualizarAnamnesis(idCita, request));
    }

    @Operation(summary = "Agregar / actualizar diagnóstico CIE-10 (A del SOAP)",
            description = "Agrega o reemplaza el diagnóstico del borrador. Valida formato CIE-10. " +
                          "Usar el catálogo GET /atenciones/cie10?q= para buscar el código.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Diagnóstico guardado",
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

    @Operation(summary = "Agregar línea de receta (P del SOAP)",
            description = "Agrega un medicamento al borrador con datos estructurados. " +
                          "Consulta antecedentes/alergias y stock como advertencia. NO descuenta stock. NO consulta precio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Línea de receta agregada",
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

    @Operation(summary = "Agregar línea de orden de laboratorio (P del SOAP)",
            description = "Agrega un examen al borrador. Consulta catálogo de ms-laboratorio. NO consulta precio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Línea de orden agregada",
                    content = @Content(schema = @Schema(implementation = AgregarOrdenResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "No existe borrador o examen no encontrado"),
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
            description = "Flujo estricto: (1) Marca cita ATENDIDA en ms-citas. " +
                          "(2) Publica EpisodioFinalizado a RabbitMQ. (3) Elimina borrador de Redis.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atención finalizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Borrador sin diagnóstico"),
            @ApiResponse(responseCode = "404", description = "No existe borrador para esta cita"),
            @ApiResponse(responseCode = "502", description = "ms-citas no disponible")
    })
    @PostMapping("/{idCita}/finalizar")
    public ResponseEntity<BorradorResponseDTO> finalizar(
            @Parameter(description = "ID de la cita", example = "100", required = true)
            @PathVariable Long idCita) {
        return ResponseEntity.ok(service.finalizar(idCita));
    }
}
