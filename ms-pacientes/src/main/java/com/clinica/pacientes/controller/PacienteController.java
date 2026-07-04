package com.clinica.pacientes.controller;

import com.clinica.pacientes.dto.AntecedenteClinicoRequestDTO;
import com.clinica.pacientes.dto.AntecedenteClinicoResponseDTO;
import com.clinica.pacientes.dto.PacienteRequestDTO;
import com.clinica.pacientes.dto.PacienteResponseDTO;
import com.clinica.pacientes.dto.PacienteUpdateRequestDTO;
import com.clinica.pacientes.service.PacienteService;
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

@Tag(name = "Pacientes", description = "Índice Maestro de Pacientes y antecedentes clínicos")
@RestController
@RequestMapping("/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @Operation(summary = "Buscar pacientes",
            description = "Búsqueda por nombre parcial o número de documento. Retorna lista vacía si no hay coincidencias.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados de búsqueda")
    })
    @GetMapping("/buscar")
    public ResponseEntity<List<PacienteResponseDTO>> buscar(
            @Parameter(description = "Término de búsqueda (nombre, apellido o documento)", example = "Torres", required = true)
            @RequestParam String q) {
        return ResponseEntity.ok(pacienteService.buscar(q));
    }

    @Operation(summary = "Registrar paciente",
            description = "Registra un nuevo paciente en el índice maestro")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Paciente registrado",
                    content = @Content(schema = @Schema(implementation = PacienteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Documento de identidad ya registrado")
    })
    @PostMapping
    public ResponseEntity<PacienteResponseDTO> registrar(
            @Valid @RequestBody PacienteRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.registrar(request));
    }

    @Operation(summary = "Consultar perfil demográfico",
            description = "Retorna los datos demográficos completos del paciente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente encontrado",
                    content = @Content(schema = @Schema(implementation = PacienteResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> obtenerPorId(
            @Parameter(description = "ID interno del paciente", example = "42", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.obtenerPorId(id));
    }

    @Operation(summary = "Verificar existencia de paciente",
            description = "Verificación ligera: 200 si existe, 404 si no. Sin body. Consumido por ms-citas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El paciente existe"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    @GetMapping("/{id}/existe")
    public ResponseEntity<Void> verificarExistencia(
            @Parameter(description = "ID interno del paciente", example = "42", required = true)
            @PathVariable Long id) {
        pacienteService.verificarExistencia(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Listar antecedentes clínicos",
            description = "Retorna las enfermedades crónicas y alergias del paciente. Consumido por ms-atencion-medica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de antecedentes"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    @GetMapping("/{id}/antecedentes")
    public ResponseEntity<List<AntecedenteClinicoResponseDTO>> obtenerAntecedentes(
            @Parameter(description = "ID interno del paciente", example = "42", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.obtenerAntecedentes(id));
    }

    @Operation(summary = "Actualizar datos del paciente",
            description = "Actualización parcial. Solo se modifican los campos presentes en el body (no nulos).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente actualizado",
                    content = @Content(schema = @Schema(implementation = PacienteResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> actualizar(
            @Parameter(description = "ID interno del paciente", example = "42", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PacienteUpdateRequestDTO request) {
        return ResponseEntity.ok(pacienteService.actualizar(id, request));
    }

    @Operation(summary = "Habilitar paciente",
            description = "Reactiva un paciente previamente deshabilitado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente habilitado"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    @PatchMapping("/{id}/habilitar")
    public ResponseEntity<PacienteResponseDTO> habilitar(
            @Parameter(description = "ID interno del paciente", example = "42", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.cambiarEstado(id, true));
    }

    @Operation(summary = "Deshabilitar paciente",
            description = "Los pacientes no se eliminan: se deshabilitan para conservar su historial clínico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente deshabilitado"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    @PatchMapping("/{id}/deshabilitar")
    public ResponseEntity<PacienteResponseDTO> deshabilitar(
            @Parameter(description = "ID interno del paciente", example = "42", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.cambiarEstado(id, false));
    }

    @Operation(summary = "Eliminar antecedente clínico",
            description = "Elimina un antecedente clínico del paciente. Valida que el antecedente pertenezca al paciente indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Antecedente eliminado"),
            @ApiResponse(responseCode = "404", description = "Paciente o antecedente no encontrado"),
            @ApiResponse(responseCode = "409", description = "El antecedente no pertenece al paciente")
    })
    @DeleteMapping("/{id}/antecedentes/{idAntecedente}")
    public ResponseEntity<Void> eliminarAntecedente(
            @Parameter(description = "ID interno del paciente", example = "42", required = true)
            @PathVariable Long id,
            @Parameter(description = "ID del antecedente a eliminar", example = "3", required = true)
            @PathVariable Long idAntecedente) {
        pacienteService.eliminarAntecedente(id, idAntecedente);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Registrar antecedente clínico",
            description = "Agrega una enfermedad crónica o alergia al historial del paciente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Antecedente registrado",
                    content = @Content(schema = @Schema(implementation = AntecedenteClinicoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    @PostMapping("/{id}/antecedentes")
    public ResponseEntity<AntecedenteClinicoResponseDTO> registrarAntecedente(
            @Parameter(description = "ID interno del paciente", example = "42", required = true)
            @PathVariable Long id,
            @Valid @RequestBody AntecedenteClinicoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pacienteService.registrarAntecedente(id, request));
    }
}
