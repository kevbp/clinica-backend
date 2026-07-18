package com.clinica.citas.controller;

import com.clinica.citas.dto.*;
import com.clinica.citas.model.EstadoCita;
import com.clinica.citas.service.CitaMedicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Citas Médicas", description = "Agendamiento, ciclo de vida y política de cancelación de citas")
@RestController
@RequestMapping("/citas")
@RequiredArgsConstructor
public class CitaMedicaController {

    private final CitaMedicaService citaService;

    @Operation(summary = "Listar citas con filtros opcionales",
            description = "Retorna citas filtradas por paciente, médico, estado y/o fecha. Todos los parámetros son opcionales y combinables.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de citas")
    })
    @GetMapping
    public ResponseEntity<List<CitaMedicaResponseDTO>> listar(
            @Parameter(description = "Filtrar por ID de paciente", example = "42")
            @RequestParam(required = false) Long idPaciente,
            @Parameter(description = "Filtrar por ID del médico", example = "5")
            @RequestParam(required = false) Long idPersonal,
            @Parameter(description = "Filtrar por estado: PENDIENTE_PAGO, CONFIRMADA, ATENDIDA, CANCELADA")
            @RequestParam(required = false) EstadoCita estado,
            @Parameter(description = "Filtrar por fecha exacta (YYYY-MM-DD)", example = "2024-07-10")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(citaService.listar(idPaciente, idPersonal, estado, fecha));
    }

    @Operation(summary = "Agenda del médico para una fecha",
            description = "Retorna todas las citas (cualquier estado) de un médico en una fecha dada. Uso típico: pantalla de agenda de recepción.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Citas del médico en la fecha")
    })
    @GetMapping("/medico/{idPersonal}")
    public ResponseEntity<List<CitaMedicaResponseDTO>> agendaMedico(
            @Parameter(description = "ID del médico", example = "5", required = true)
            @PathVariable Long idPersonal,
            @Parameter(description = "Fecha de la agenda (YYYY-MM-DD)", example = "2024-07-10", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(citaService.listar(null, idPersonal, null, fecha));
    }

    @Operation(summary = "Consultar cita por ID",
            description = "Retorna el estado actual de la cita. Consumido por ms-atencion-medica para validar CONFIRMADA al iniciar una atención.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cita encontrada",
                    content = @Content(schema = @Schema(implementation = CitaMedicaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CitaMedicaResponseDTO> obtenerPorId(
            @Parameter(description = "ID de la cita", example = "100", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerPorId(id));
    }

    @Operation(summary = "Consultar disponibilidad (Lazy Evaluation)",
            description = "Calcula bloques de 20 minutos disponibles para un médico en una fecha, " +
                          "en memoria: turnos maestros de ms-horarios menos citas ya ocupadas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de slots disponibles")
    })
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<SlotDisponibleDTO>> disponibilidad(
            @Parameter(description = "ID del médico", example = "5", required = true)
            @RequestParam Long idPersonal,
            @Parameter(description = "Fecha a consultar (YYYY-MM-DD)", example = "2024-07-10", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(citaService.calcularDisponibilidad(idPersonal, fecha));
    }

    @Operation(summary = "Crear cita",
            description = "Valida: médico habilitado (ms-personal), existencia del paciente (ms-pacientes), " +
                          "que la fechaHora cae en un turno del médico (ms-horarios) y que el slot esté libre. " +
                          "Persiste en PENDIENTE_PAGO y publica evento CitaCreada hacia RabbitMQ.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cita creada en PENDIENTE_PAGO",
                    content = @Content(schema = @Schema(implementation = CitaMedicaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Médico no habilitado, slot inválido o datos incorrectos"),
            @ApiResponse(responseCode = "404", description = "Médico o paciente no encontrado"),
            @ApiResponse(responseCode = "409", description = "Slot ya ocupado"),
            @ApiResponse(responseCode = "502", description = "Dependencia (ms-personal / ms-pacientes / ms-horarios) no disponible")
    })
    @PostMapping
    public ResponseEntity<CitaMedicaResponseDTO> crear(
            @Valid @RequestBody CitaMedicaRequestDTO request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.crear(request, authHeader));
    }

    @Operation(summary = "Cambiar estado de la cita",
            description = "Transiciones válidas: PENDIENTE_PAGO→CONFIRMADA (invocado por ms-caja), " +
                          "CONFIRMADA→ATENDIDA (invocado por ms-atencion-medica).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado",
                    content = @Content(schema = @Schema(implementation = CitaMedicaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<CitaMedicaResponseDTO> actualizarEstado(
            @Parameter(description = "ID de la cita", example = "100", required = true)
            @PathVariable Long id,
            @Valid @RequestBody EstadoUpdateRequestDTO request) {
        return ResponseEntity.ok(citaService.actualizarEstado(id, request));
    }

    @Operation(summary = "Cancelar cita PENDIENTE_PAGO",
            description = "Cancelación libre sin penalidad — nunca hubo pago. " +
                          "Solo permitido sobre citas en estado PENDIENTE_PAGO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cita cancelada"),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada"),
            @ApiResponse(responseCode = "409", description = "La cita no está en PENDIENTE_PAGO")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CitaMedicaResponseDTO> cancelarPendientePago(
            @Parameter(description = "ID de la cita", example = "100", required = true)
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(citaService.cancelarPendientePago(id, authHeader));
    }

    @Operation(summary = "Cancelar cita CONFIRMADA (por el paciente)",
            description = "Aplica la política de cancelación según anticipación: " +
                          "≥24h → nota de crédito por el 100 % del pago (CANCELACION_ANTICIPADA). " +
                          "<24h → nota de crédito por el 70 % del pago; el 30 % se retiene como penalidad (CANCELACION_TARDIA). " +
                          "Siempre emite nota de crédito mientras el pago esté en estado PAGADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cita cancelada y nota de crédito emitida",
                    content = @Content(schema = @Schema(implementation = CitaMedicaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada"),
            @ApiResponse(responseCode = "409", description = "La cita no está en CONFIRMADA"),
            @ApiResponse(responseCode = "502", description = "ms-caja no disponible para emitir nota de crédito")
    })
    @PostMapping("/{id}/cancelar-confirmada")
    public ResponseEntity<CitaMedicaResponseDTO> cancelarConfirmada(
            @Parameter(description = "ID de la cita", example = "100", required = true)
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(citaService.cancelarConfirmada(id, authHeader));
    }

    @Operation(summary = "Cancelar cita CONFIRMADA por parte de la clínica",
            description = "La clínica cancela por causas de fuerza mayor (ausencia del médico, falla técnica, cierre imprevisto). " +
                          "Siempre emite nota de crédito por el 100 % del pago sin importar la anticipación (CANCELACION_POR_CLINICA).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cita cancelada y nota de crédito total emitida",
                    content = @Content(schema = @Schema(implementation = CitaMedicaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada"),
            @ApiResponse(responseCode = "409", description = "La cita no está en CONFIRMADA"),
            @ApiResponse(responseCode = "502", description = "ms-caja no disponible para emitir nota de crédito")
    })
    @PostMapping("/{id}/cancelar-por-clinica")
    public ResponseEntity<CitaMedicaResponseDTO> cancelarPorClinica(
            @Parameter(description = "ID de la cita", example = "100", required = true)
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(citaService.cancelarPorClinica(id, authHeader));
    }

    @Operation(summary = "Reagendar cita",
            description = "Cambia la fechaHora de una cita en estado PENDIENTE_PAGO o CONFIRMADA. " +
                          "Si está CONFIRMADA requiere ≥24h de anticipación. " +
                          "El nuevo slot debe coincidir con un turno disponible del médico. " +
                          "Publica evento CitaReagendada hacia ms-notificaciones.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cita reagendada",
                    content = @Content(schema = @Schema(implementation = CitaMedicaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Anticipación insuficiente (<24h) para cita CONFIRMADA"),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada"),
            @ApiResponse(responseCode = "409", description = "Estado inválido (no es PENDIENTE_PAGO ni CONFIRMADA) o el nuevo slot está ocupado")
    })
    @PostMapping("/{id}/reagendar")
    public ResponseEntity<CitaMedicaResponseDTO> reagendar(
            @Parameter(description = "ID de la cita", example = "100", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ReagendarRequestDTO request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(citaService.reagendar(id, request, authHeader));
    }

    @Operation(summary = "Compensación de Saga — pago fallido",
            description = "Paso de compensación de la Saga de agendamiento+pago (ver sagas.md §14.1). " +
                          "Invocado por ms-caja si, tras marcar el pago como PAGADO, la confirmación de la cita falla " +
                          "tras reintentos. Cancela la cita y libera el slot sin emitir NotaCredito " +
                          "(el pago queda en PAGADO_SIN_CONFIRMAR para revisión administrativa).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cita cancelada por compensación"),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    @PostMapping("/{id}/compensar-pago-fallido")
    public ResponseEntity<CitaMedicaResponseDTO> compensarPagoFallido(
            @Parameter(description = "ID de la cita", example = "100", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(citaService.compensarPagoFallido(id));
    }
}
