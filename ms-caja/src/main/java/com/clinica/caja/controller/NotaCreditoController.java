package com.clinica.caja.controller;

import com.clinica.caja.dto.NotaCreditoRequestDTO;
import com.clinica.caja.dto.NotaCreditoResponseDTO;
import com.clinica.caja.service.NotaCreditoService;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "Notas de Crédito",
        description = "Emitidas por ms-citas al cancelar una cita CONFIRMADA dentro de la ventana de 24h")
@RestController
@RequestMapping("/notas-credito")
@RequiredArgsConstructor
public class NotaCreditoController {

    private final NotaCreditoService notaCreditoService;

    @Operation(summary = "Emitir nota de crédito",
            description = "Invocado síncronamente por ms-citas al cancelar una cita CONFIRMADA " +
                          "con ≥24h de anticipación. El monto es igual al PagoConsulta original.")
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
}
