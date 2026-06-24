package com.clinica.caja.controller;

import com.clinica.caja.dto.PagoConsultaRequestDTO;
import com.clinica.caja.dto.PagoConsultaResponseDTO;
import com.clinica.caja.service.PagoConsultaService;
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

@Tag(name = "Pago de Consulta",
        description = "Cobro previo al acto médico. Orquesta la Saga 14.1 de agendamiento+pago.")
@RestController
@RequestMapping("/pagos-consulta")
@RequiredArgsConstructor
public class PagoConsultaController {

    private final PagoConsultaService pagoService;

    @Operation(summary = "Consultar pago de consulta por ID",
            description = "Retorna el estado actual del pago: PENDIENTE, PAGADO o PAGADO_SIN_CONFIRMAR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago encontrado",
                    content = @Content(schema = @Schema(implementation = PagoConsultaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "PagoConsulta no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PagoConsultaResponseDTO> obtenerPorId(
            @Parameter(description = "ID del PagoConsulta", example = "10", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    @Operation(summary = "Consultar pago de consulta por ID de cita",
            description = "Permite a caja localizar el cobro pendiente de una cita sin conocer el ID del PagoConsulta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago encontrado",
                    content = @Content(schema = @Schema(implementation = PagoConsultaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "No existe pago para esa cita")
    })
    @GetMapping("/cita/{idCita}")
    public ResponseEntity<PagoConsultaResponseDTO> obtenerPorCita(
            @Parameter(description = "ID de la cita médica", example = "100", required = true)
            @PathVariable Long idCita) {
        return ResponseEntity.ok(pagoService.obtenerPorCita(idCita));
    }

    @Operation(summary = "Crear cobro pendiente de consulta",
            description = "Consulta la especialidad del médico (ms-personal) para calcular el monto " +
                          "según TarifaConsulta y persiste el pago en estado PENDIENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago pendiente creado",
                    content = @Content(schema = @Schema(implementation = PagoConsultaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Tarifa de especialidad no encontrada"),
            @ApiResponse(responseCode = "409", description = "Ya existe pago para esta cita"),
            @ApiResponse(responseCode = "502", description = "ms-personal no disponible")
    })
    @PostMapping
    public ResponseEntity<PagoConsultaResponseDTO> crear(
            @Valid @RequestBody PagoConsultaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.crear(request));
    }

    @Operation(summary = "Confirmar pago — Saga 14.1",
            description = "Paso 3 (Saga §14.1): marca PagoConsulta como PAGADO (transacción local). " +
                          "Paso 4: llama síncronamente a ms-citas (CONFIRMADA) con hasta 3 reintentos. " +
                          "Si todos los reintentos fallan: pago queda PAGADO_SIN_CONFIRMAR y se llama " +
                          "a POST /citas/{id}/compensar-pago-fallido. " +
                          "Si todo sale bien: publica PagoConsultaConfirmado hacia RabbitMQ.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago confirmado o compensación ejecutada",
                    content = @Content(schema = @Schema(implementation = PagoConsultaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "PagoConsulta no encontrado"),
            @ApiResponse(responseCode = "409", description = "El pago no está en estado PENDIENTE")
    })
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<PagoConsultaResponseDTO> confirmar(
            @Parameter(description = "ID del PagoConsulta", example = "10", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(pagoService.confirmarPago(id));
    }
}
