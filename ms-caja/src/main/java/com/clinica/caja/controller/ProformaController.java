package com.clinica.caja.controller;

import com.clinica.caja.dto.ComprobanteResponseDTO;
import com.clinica.caja.dto.ConstruirProformaRequestDTO;
import com.clinica.caja.dto.PagarItemsRequestDTO;
import com.clinica.caja.dto.ProformaResponseDTO;
import com.clinica.caja.service.ProformaService;
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
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@Tag(name = "Proformas", description = "Cobro post-atencion de receta y examenes. Orquesta la Saga 14.2.")
@RestController
@RequestMapping("/proformas")
@RequiredArgsConstructor
public class ProformaController {

    private final ProformaService proformaService;

    @Operation(summary = "Consultar proforma por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proforma encontrada",
                    content = @Content(schema = @Schema(implementation = ProformaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProformaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(proformaService.obtenerPorId(id));
    }

    @Operation(summary = "Listar proformas de un paciente")
    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<List<ProformaResponseDTO>> listarPorPaciente(@PathVariable Long idPaciente) {
        return ResponseEntity.ok(proformaService.listarPorPaciente(idPaciente));
    }

    @Operation(summary = "Listar proformas generadas desde una receta especifica")
    @GetMapping("/receta/{idReceta}")
    public ResponseEntity<List<ProformaResponseDTO>> listarPorReceta(@PathVariable String idReceta) {
        return ResponseEntity.ok(proformaService.listarPorReceta(idReceta));
    }

    @Operation(summary = "Listar proformas generadas desde una orden de laboratorio especifica")
    @GetMapping("/orden/{idOrden}")
    public ResponseEntity<List<ProformaResponseDTO>> listarPorOrden(@PathVariable String idOrden) {
        return ResponseEntity.ok(proformaService.listarPorOrden(idOrden));
    }

    @Operation(summary = "Construir proforma de MEDICAMENTOS desde una receta",
            description = "Congela precios de cada medicamento prescrito. Vigencia: 7 dias. " +
                          "Si la receta ya tiene una proforma vigente, se genera una nueva igualmente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Proforma construida",
                    content = @Content(schema = @Schema(implementation = ProformaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "La receta no tiene medicamentos"),
            @ApiResponse(responseCode = "404", description = "Receta no encontrada"),
            @ApiResponse(responseCode = "502", description = "ms-farmacia o ms-historias-clinicas no disponible")
    })
    @PostMapping("/receta/{idReceta}")
    public ResponseEntity<ProformaResponseDTO> construirDesdeReceta(
            @PathVariable String idReceta,
            @Valid @RequestBody ConstruirProformaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proformaService.construirDesdeReceta(idReceta, request));
    }

    @Operation(summary = "Construir proforma de EXAMENES desde una orden de laboratorio",
            description = "Congela precios de cada examen ordenado. Vigencia: 7 dias.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Proforma construida",
                    content = @Content(schema = @Schema(implementation = ProformaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "La orden no tiene examenes"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
            @ApiResponse(responseCode = "502", description = "ms-laboratorio o ms-historias-clinicas no disponible")
    })
    @PostMapping("/orden/{idOrden}")
    public ResponseEntity<ProformaResponseDTO> construirDesdeOrden(
            @PathVariable String idOrden,
            @Valid @RequestBody ConstruirProformaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proformaService.construirDesdeOrden(idOrden, request));
    }

    @Operation(summary = "Pagar items seleccionados - Saga 14.2",
            description = "Cada item se procesa como transaccion local independiente. " +
                          "MEDICAMENTO: descuenta stock en ms-farmacia (FEFO). " +
                          "EXAMEN: crea ExamenAutorizado en ms-laboratorio. " +
                          "Fallo de negocio (sin stock) -> item NO_DISPONIBLE. Fallo tecnico -> item PENDIENTE. " +
                          "Devuelve 410 (GONE) si la proforma ya expiro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Items procesados (revisar estado de cada uno)"),
            @ApiResponse(responseCode = "404", description = "Proforma no encontrada"),
            @ApiResponse(responseCode = "409", description = "Proforma ya pagada"),
            @ApiResponse(responseCode = "410", description = "Proforma expirada, genere una nueva")
    })
    @PostMapping("/{id}/pagar-items")
    public ResponseEntity<ProformaResponseDTO> pagarItems(
            @PathVariable Long id,
            @Valid @RequestBody PagarItemsRequestDTO request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(proformaService.pagarItems(id, request, authHeader));
    }

    @Operation(summary = "Emitir comprobante",
            description = "Genera la boleta definitiva sobre los items efectivamente pagados.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comprobante emitido",
                    content = @Content(schema = @Schema(implementation = ComprobanteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "No hay items pagados"),
            @ApiResponse(responseCode = "404", description = "Proforma no encontrada")
    })
    @PostMapping("/{id}/emitir-comprobante")
    public ResponseEntity<ComprobanteResponseDTO> emitirComprobante(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proformaService.emitirComprobante(id, authHeader));
    }
}