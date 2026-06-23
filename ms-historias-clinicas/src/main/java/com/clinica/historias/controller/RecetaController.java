package com.clinica.historias.controller;

import com.clinica.historias.dto.RecetaResponseDTO;
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

@Tag(name = "Recetas",
        description = "Recetas médicas emitidas. Consulta de solo lectura — consumida por ms-caja para construir la proforma.")
@RestController
@RequestMapping("/recetas")
@RequiredArgsConstructor
public class RecetaController {

    private final HistoriasClinicasService service;

    @Operation(summary = "Listar recetas de un paciente",
            description = "Retorna todas las recetas del paciente con sus líneas de medicamentos. " +
                          "Consumido por ms-caja para construir la proforma.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de recetas")
    })
    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<List<RecetaResponseDTO>> listarPorPaciente(
            @Parameter(description = "ID del paciente en ms-pacientes", example = "42", required = true)
            @PathVariable Long idPaciente) {
        return ResponseEntity.ok(service.listarRecetasPorPaciente(idPaciente));
    }
}
