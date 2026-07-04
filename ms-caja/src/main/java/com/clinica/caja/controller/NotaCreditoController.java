package com.clinica.caja.controller;

import com.clinica.caja.dto.EnviarCorreoRequestDTO;
import com.clinica.caja.dto.NotaCreditoRequestDTO;
import com.clinica.caja.dto.NotaCreditoResponseDTO;
import com.clinica.caja.service.NotaCreditoService;
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

@Tag(name = "Notas de Crédito",
        description = "Emitidas por ms-citas al cancelar una cita CONFIRMADA dentro de la ventana de 24h")
@RestController
@RequestMapping("/notas-credito")
@RequiredArgsConstructor
public class NotaCreditoController {

    private final NotaCreditoService notaCreditoService;

    @Operation(summary = "Listar notas de crédito por paciente",
            description = "Retorna el historial de notas de crédito disponibles para un paciente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de notas de crédito")
    })
    @GetMapping
    public ResponseEntity<List<NotaCreditoResponseDTO>> listarPorPaciente(
            @Parameter(description = "ID del paciente", example = "42", required = true)
            @RequestParam Long idPaciente) {
        return ResponseEntity.ok(notaCreditoService.listarPorPaciente(idPaciente));
    }

    @Operation(summary = "Emitir nota de crédito",
            description = "Invocado síncronamente por ms-citas al cancelar una cita CONFIRMADA. " +
                          "El tipo determina el porcentaje: CANCELACION_ANTICIPADA/CANCELACION_POR_CLINICA/ERROR_COBRO " +
                          "→ 100 %; CANCELACION_TARDIA/NO_SHOW → 70 % (penalidad 30 %).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Nota de crédito emitida",
                    content = @Content(schema = @Schema(implementation = NotaCreditoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "No existe pago de consulta para esa cita"),
            @ApiResponse(responseCode = "409", description = "El pago no está en estado PAGADO")
    })
    @PostMapping
    public ResponseEntity<NotaCreditoResponseDTO> emitir(
            @Valid @RequestBody NotaCreditoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notaCreditoService.emitir(request));
    }

    @Operation(summary = "Enviar NC por correo electrónico")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Envío solicitado"),
            @ApiResponse(responseCode = "404", description = "NC no encontrada")
    })
    @PostMapping("/{id}/enviar-correo")
    public ResponseEntity<Void> enviarPorCorreo(
            @PathVariable Long id,
            @Valid @RequestBody EnviarCorreoRequestDTO request) {
        notaCreditoService.enviarPorCorreo(id, request.getCorreo());
        return ResponseEntity.noContent().build();
    }
}
