package com.clinica.laboratorio.controller;

import com.clinica.laboratorio.dto.ExamenAutorizadoRequestDTO;
import com.clinica.laboratorio.dto.ExamenAutorizadoResponseDTO;
import com.clinica.laboratorio.service.ExamenService;
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

@Tag(name = "Exámenes Autorizados",
        description = "Autorizaciones de examen creadas únicamente al confirmar el pago en ms-caja")
@RestController
@RequestMapping("/examenes-autorizados")
@RequiredArgsConstructor
public class ExamenAutorizadoController {

    private final ExamenService examenService;

    @Operation(summary = "Listar exámenes autorizados por paciente",
            description = "Retorna todas las autorizaciones de examen de un paciente. Útil para que el laboratorio vea qué exámenes puede procesar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de autorizaciones")
    })
    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<List<ExamenAutorizadoResponseDTO>> listarPorPaciente(
            @Parameter(description = "ID del paciente en ms-pacientes", example = "42", required = true)
            @PathVariable Long idPaciente) {
        return ResponseEntity.ok(examenService.listarAutorizadosPorPaciente(idPaciente));
    }

    @Operation(summary = "Autorizar examen",
            description = "Crea el registro de autorización que habilita el procesamiento técnico del examen. " +
                          "Invocado ÚNICAMENTE por ms-caja en el momento en que el paciente paga ese examen específico. " +
                          "No existe antes del pago — la indicación previa solo vive como OrdenLaboratorio en ms-historias-clinicas.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Examen autorizado",
                    content = @Content(schema = @Schema(implementation = ExamenAutorizadoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Examen no encontrado en el catálogo")
    })
    @PostMapping
    public ResponseEntity<ExamenAutorizadoResponseDTO> autorizar(
            @Valid @RequestBody ExamenAutorizadoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(examenService.autorizar(request));
    }
}
