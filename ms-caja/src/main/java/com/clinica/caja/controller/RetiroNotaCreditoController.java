package com.clinica.caja.controller;

import com.clinica.caja.dto.RetiroRequestDTO;
import com.clinica.caja.dto.RetiroResponseDTO;
import com.clinica.caja.service.RetiroNotaCreditoService;
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

@Tag(name = "Retiros de Nota de Crédito",
        description = "Solicitar y consultar retiros bancarios de notas de crédito disponibles")
@RestController
@RequestMapping("/retiros-nota-credito")
@RequiredArgsConstructor
public class RetiroNotaCreditoController {

    private final RetiroNotaCreditoService retiroService;

    @Operation(summary = "Solicitar retiro bancario de una NC",
            description = "Marca la NC como USADA y registra la solicitud de transferencia bancaria. " +
                          "Envía email de confirmación si se provee correo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Retiro solicitado",
                    content = @Content(schema = @Schema(implementation = RetiroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "NC no encontrada"),
            @ApiResponse(responseCode = "409", description = "NC no disponible o ya tiene retiro registrado")
    })
    @PostMapping
    public ResponseEntity<RetiroResponseDTO> solicitar(
            @Parameter(description = "ID del paciente", required = true)
            @RequestParam Long idPaciente,
            @Valid @RequestBody RetiroRequestDTO request,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(retiroService.solicitarRetiro(idPaciente, request, authHeader));
    }

    @Operation(summary = "Listar retiros de un paciente")
    @GetMapping
    public ResponseEntity<List<RetiroResponseDTO>> listar(
            @Parameter(description = "ID del paciente", required = true)
            @RequestParam Long idPaciente) {
        return ResponseEntity.ok(retiroService.listarPorPaciente(idPaciente));
    }
}
