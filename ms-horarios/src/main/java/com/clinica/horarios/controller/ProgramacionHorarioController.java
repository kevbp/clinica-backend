package com.clinica.horarios.controller;

import com.clinica.horarios.dto.ProgramacionHorarioRequestDTO;
import com.clinica.horarios.dto.ProgramacionHorarioResponseDTO;
import com.clinica.horarios.service.ProgramacionHorarioService;
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

@Tag(name = "Programación de Horarios",
        description = "Turnos maestros de los que ms-citas deriva los bloques de cita disponibles")
@RestController
@RequestMapping("/programacion-horarios")
@RequiredArgsConstructor
public class ProgramacionHorarioController {

    private final ProgramacionHorarioService programacionHorarioService;

    @Operation(summary = "Crear turno maestro",
            description = "Registra una franja horaria para un médico en un consultorio. " +
                          "Valida que el consultorio no esté asignado a otro personal en la misma franja.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Turno maestro creado",
                    content = @Content(schema = @Schema(implementation = ProgramacionHorarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o rango horario incorrecto"),
            @ApiResponse(responseCode = "404", description = "Consultorio no encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflicto: el consultorio ya está ocupado en esa franja por otro personal")
    })
    @PostMapping
    public ResponseEntity<ProgramacionHorarioResponseDTO> crear(
            @Valid @RequestBody ProgramacionHorarioRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(programacionHorarioService.crear(request));
    }

    @Operation(summary = "Obtener turnos de un médico",
            description = "Lista todos los turnos maestros de un médico. " +
                          "Consumido por ms-citas para calcular disponibilidad mediante Lazy Evaluation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de turnos del médico")
    })
    @GetMapping("/personal/{idPersonal}")
    public ResponseEntity<List<ProgramacionHorarioResponseDTO>> obtenerPorPersonal(
            @Parameter(description = "ID del personal en ms-personal", example = "5", required = true)
            @PathVariable Long idPersonal) {
        return ResponseEntity.ok(programacionHorarioService.obtenerPorPersonal(idPersonal));
    }
}
