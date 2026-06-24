package com.clinica.historias.controller;

import com.clinica.historias.dto.OrdenLaboratorioResponseDTO;
import com.clinica.historias.service.HistoriasClinicasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Órdenes de Laboratorio",
        description = "Órdenes de laboratorio emitidas. Consulta de solo lectura — consumida por ms-caja para construir la proforma.")
@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
public class OrdenController {

    private final HistoriasClinicasService service;

    @Operation(summary = "Obtener orden de laboratorio por ID",
            description = "Retorna el detalle completo de una orden. Útil para la vista de impresión.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden encontrada"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrdenLaboratorioResponseDTO> obtenerPorId(
            @Parameter(description = "ID interno de la orden (ObjectId MongoDB)", example = "664a1b2c3d4e5f6a7b8c9d0f", required = true)
            @PathVariable String id) {
        return ResponseEntity.ok(service.obtenerOrdenPorId(id));
    }

    @Operation(summary = "Listar órdenes de laboratorio de un paciente",
            description = "Retorna todas las órdenes del paciente con sus líneas de exámenes. " +
                          "Consumido por ms-caja para construir la proforma.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de órdenes")
    })
    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<List<OrdenLaboratorioResponseDTO>> listarPorPaciente(
            @Parameter(description = "ID del paciente en ms-pacientes", example = "42", required = true)
            @PathVariable Long idPaciente) {
        return ResponseEntity.ok(service.listarOrdenesPorPaciente(idPaciente));
    }
}
