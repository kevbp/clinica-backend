package com.clinica.caja.controller;

import com.clinica.caja.dto.TarifaConsultaRequestDTO;
import com.clinica.caja.dto.TarifaConsultaResponseDTO;
import com.clinica.caja.service.TarifaConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tarifas de Consulta", description = "Catálogo de montos por especialidad médica")
@RestController
@RequestMapping("/tarifas-consulta")
@RequiredArgsConstructor
public class TarifaConsultaController {

    private final TarifaConsultaService tarifaService;

    @Operation(summary = "Registrar o actualizar tarifa de especialidad")
    @PostMapping
    public ResponseEntity<TarifaConsultaResponseDTO> guardar(
            @Valid @RequestBody TarifaConsultaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tarifaService.guardar(request));
    }

    @Operation(summary = "Listar todas las tarifas")
    @GetMapping
    public ResponseEntity<List<TarifaConsultaResponseDTO>> listar() {
        return ResponseEntity.ok(tarifaService.listar());
    }
}
