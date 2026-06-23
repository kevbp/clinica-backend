package com.clinica.caja.controller;

import com.clinica.caja.dto.ComprobanteResponseDTO;
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

@Tag(name = "Proformas",
        description = "Cobro post-atención de receta y exámenes. Orquesta la Saga 14.2.")
@RestController
@RequestMapping("/proformas")
@RequiredArgsConstructor
public class ProformaController {

    private final ProformaService proformaService;

    @Operation(summary = "Construir proforma con precios congelados",
            description = "Consulta recetas y órdenes de ms-historias-clinicas, obtiene precios " +
                          "vigentes de ms-farmacia y ms-laboratorio y los congela en ItemProforma. " +
                          "El precio no cambia aunque el catálogo se actualice después.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Proforma construida",
                    content = @Content(schema = @Schema(implementation = ProformaResponseDTO.class))),
            @ApiResponse(responseCode = "502", description = "Dependencia no disponible")
    })
    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<ProformaResponseDTO> construir(
            @Parameter(description = "ID del paciente", example = "42", required = true)
            @PathVariable Long idPaciente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proformaService.construir(idPaciente));
    }

    @Operation(summary = "Pagar ítems seleccionados — Saga 14.2",
            description = "Cada ítem se procesa como transacción local independiente. " +
                          "MEDICAMENTO: llama descontar-stock en ms-farmacia. EXAMEN: llama examenes-autorizados en ms-laboratorio. " +
                          "Fallo de negocio (sin stock) → ítem NO_DISPONIBLE. Fallo técnico → ítem queda PENDIENTE. " +
                          "Ningún fallo afecta a los ítems ya confirmados de la misma proforma.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ítems procesados (revisar estado de cada uno)",
                    content = @Content(schema = @Schema(implementation = ProformaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Proforma no encontrada")
    })
    @PostMapping("/{id}/pagar-items")
    public ResponseEntity<ProformaResponseDTO> pagarItems(
            @Parameter(description = "ID de la proforma", example = "5", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PagarItemsRequestDTO request) {
        return ResponseEntity.ok(proformaService.pagarItems(id, request));
    }

    @Operation(summary = "Emitir comprobante",
            description = "Genera la boleta/factura definitiva sobre los ítems efectivamente pagados.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comprobante emitido",
                    content = @Content(schema = @Schema(implementation = ComprobanteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "No hay ítems pagados en la proforma"),
            @ApiResponse(responseCode = "404", description = "Proforma no encontrada")
    })
    @PostMapping("/{id}/emitir-comprobante")
    public ResponseEntity<ComprobanteResponseDTO> emitirComprobante(
            @Parameter(description = "ID de la proforma", example = "5", required = true)
            @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proformaService.emitirComprobante(id));
    }
}
