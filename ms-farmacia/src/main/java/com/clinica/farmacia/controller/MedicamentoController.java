package com.clinica.farmacia.controller;

import com.clinica.farmacia.dto.*;
import com.clinica.farmacia.service.MedicamentoService;
import java.util.List;
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

@Tag(name = "Medicamentos", description = "Catálogo maestro, inventario por lote y operaciones de stock")
@RestController
@RequestMapping("/medicamentos")
@RequiredArgsConstructor
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    @Operation(summary = "Listar / buscar catálogo de medicamentos",
            description = "Sin parámetros retorna todos los medicamentos. Con `q` filtra por nombre o principio activo (parcial, sin importar mayúsculas).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de medicamentos del catálogo")
    })
    @GetMapping
    public ResponseEntity<List<MedicamentoResponseDTO>> listar(
            @Parameter(description = "Término de búsqueda: nombre o principio activo", example = "Amoxicilina")
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(medicamentoService.listarCatalogo(q));
    }

    @Operation(summary = "Registrar medicamento",
            description = "Agrega un medicamento al catálogo con su precio de venta")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Medicamento registrado",
                    content = @Content(schema = @Schema(implementation = MedicamentoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<MedicamentoResponseDTO> crear(
            @Valid @RequestBody MedicamentoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicamentoService.crear(request));
    }

    @Operation(summary = "Consultar medicamento por ID",
            description = "Retorna el medicamento sin precio ni detalle de inventario por lote")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medicamento encontrado",
                    content = @Content(schema = @Schema(implementation = MedicamentoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Medicamento no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MedicamentoResponseDTO> obtenerCatalogo(
            @Parameter(description = "ID interno del medicamento", example = "104", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(medicamentoService.obtenerCatalogo(id));
    }

    @Operation(summary = "Consultar precio vigente",
            description = "Retorna el precio de venta. Endpoint EXCLUSIVO para ms-caja — " +
                          "ningún otro servicio debe consumirlo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Precio encontrado",
                    content = @Content(schema = @Schema(implementation = PrecioResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Medicamento no encontrado")
    })
    @GetMapping("/{id}/precio")
    public ResponseEntity<PrecioResponseDTO> obtenerPrecio(
            @Parameter(description = "ID interno del medicamento", example = "104", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(medicamentoService.obtenerPrecio(id));
    }

    @Operation(summary = "Consultar disponibilidad de stock",
            description = "Stock teórico agregado sobre lotes no vencidos. Solo lectura — " +
                          "consumido por ms-atencion-medica para advertencia al médico. Nunca descuenta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad calculada",
                    content = @Content(schema = @Schema(implementation = DisponibilidadResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Medicamento no encontrado")
    })
    @GetMapping("/{id}/disponibilidad")
    public ResponseEntity<DisponibilidadResponseDTO> obtenerDisponibilidad(
            @Parameter(description = "ID interno del medicamento", example = "104", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(medicamentoService.obtenerDisponibilidad(id));
    }

    @Operation(summary = "Actualizar medicamento",
            description = "Actualización parcial. Solo se modifican los campos no nulos. El precio actualizado afecta solo a nuevas proformas (las existentes tienen precio congelado).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medicamento actualizado",
                    content = @Content(schema = @Schema(implementation = MedicamentoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Medicamento no encontrado")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<MedicamentoResponseDTO> actualizar(
            @Parameter(description = "ID interno del medicamento", example = "104", required = true)
            @PathVariable Long id,
            @RequestBody MedicamentoUpdateRequestDTO request) {
        return ResponseEntity.ok(medicamentoService.actualizar(id, request));
    }

    @Operation(summary = "Listar lotes de un medicamento",
            description = "Retorna todos los lotes registrados con su stock actual. Útil para el panel de administración de farmacia.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de lotes"),
            @ApiResponse(responseCode = "404", description = "Medicamento no encontrado")
    })
    @GetMapping("/{id}/lotes")
    public ResponseEntity<List<LoteResponseDTO>> listarLotes(
            @Parameter(description = "ID interno del medicamento", example = "104", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(medicamentoService.listarLotes(id));
    }

    @Operation(summary = "Agregar lote con stock inicial",
            description = "Registra un nuevo lote y su inventario inicial para un medicamento existente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lote registrado",
                    content = @Content(schema = @Schema(implementation = LoteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Medicamento no encontrado")
    })
    @PostMapping("/{id}/lotes")
    public ResponseEntity<LoteResponseDTO> agregarLote(
            @Parameter(description = "ID interno del medicamento", example = "104", required = true)
            @PathVariable Long id,
            @Valid @RequestBody LoteRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicamentoService.agregarLote(id, request));
    }

    @Operation(summary = "Descontar stock (FEFO)",
            description = "Efecto de escritura real sobre el inventario. Invocado ÚNICAMENTE por ms-caja " +
                          "al confirmar el pago de ese ítem. Aplica FEFO (lote con vencimiento más próximo primero). " +
                          "Si el stock es insuficiente, responde exitoso=false sin lanzar excepción " +
                          "para que ms-caja pueda marcar el ítem como NO_DISPONIBLE sin abortar la transacción.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultado del descuento (exitoso o fallo controlado)",
                    content = @Content(schema = @Schema(implementation = DescontarStockResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Medicamento no encontrado")
    })
    @PatchMapping("/{id}/descontar-stock")
    public ResponseEntity<DescontarStockResponseDTO> descontarStock(
            @Parameter(description = "ID interno del medicamento", example = "104", required = true)
            @PathVariable Long id,
            @Valid @RequestBody DescontarStockRequestDTO request) {
        return ResponseEntity.ok(medicamentoService.descontarStock(id, request));
    }
}
