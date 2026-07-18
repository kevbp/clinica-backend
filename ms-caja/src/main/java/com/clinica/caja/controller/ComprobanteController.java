package com.clinica.caja.controller;

import com.clinica.caja.dto.ComprobanteResponseDTO;
import com.clinica.caja.dto.EnviarCorreoRequestDTO;
import com.clinica.caja.service.ComprobanteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comprobantes", description = "Historial de boletas emitidas (pago de consulta y proforma)")
@RestController
@RequestMapping("/comprobantes")
@RequiredArgsConstructor
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

    @Operation(summary = "Listar comprobantes de un paciente",
            description = "Une boletas de CONSULTA (pago de consulta) y PROFORMA, ordenadas por fecha de emisión descendente.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Lista de comprobantes") })
    @GetMapping
    public ResponseEntity<List<ComprobanteResponseDTO>> listarPorPaciente(
            @Parameter(description = "ID del paciente", example = "42", required = true)
            @RequestParam Long idPaciente) {
        return ResponseEntity.ok(comprobanteService.listarPorPaciente(idPaciente));
    }

    @Operation(summary = "Consultar comprobante por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comprobante encontrado"),
            @ApiResponse(responseCode = "404", description = "Comprobante no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ComprobanteResponseDTO> obtenerPorId(
            @Parameter(description = "ID del comprobante", example = "7", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(comprobanteService.obtenerPorId(id));
    }

    @Operation(summary = "Reenviar comprobante por correo electrónico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud de envío encolada"),
            @ApiResponse(responseCode = "400", description = "Correo inválido"),
            @ApiResponse(responseCode = "404", description = "Comprobante no encontrado")
    })
    @PostMapping("/{id}/enviar-correo")
    public ResponseEntity<Void> enviarPorCorreo(
            @Parameter(description = "ID del comprobante", example = "7", required = true)
            @PathVariable Long id,
            @RequestBody @Valid EnviarCorreoRequestDTO request,
            @RequestHeader("Authorization") String authHeader) {
        comprobanteService.enviarPorCorreo(id, request.getCorreo(), authHeader);
        return ResponseEntity.ok().build();
    }
}
