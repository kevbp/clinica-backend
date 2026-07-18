package com.clinica.personal.controller;

import com.clinica.personal.dto.PersonalMedicoRequestDTO;
import com.clinica.personal.dto.PersonalMedicoResponseDTO;
import com.clinica.personal.dto.PersonalRequestDTO;
import com.clinica.personal.dto.PersonalResponseDTO;
import com.clinica.personal.dto.PersonalUpdateRequestDTO;
import com.clinica.personal.model.TipoPersonal;
import com.clinica.personal.service.PersonalService;
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

@Tag(name = "Personal", description = "Gestión del talento humano de la clínica")
@RestController
@RequiredArgsConstructor
@RequestMapping("/personal")
public class PersonalController {

    private final PersonalService personalService;

    @Operation(summary = "Registrar personal", description = "Registra un nuevo miembro del personal (cualquier tipo)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Personal registrado",
                    content = @Content(schema = @Schema(implementation = PersonalResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Documento de identidad o keycloakUserId ya registrado")
    })
    @PostMapping
    public ResponseEntity<PersonalResponseDTO> registrar(
            @Valid @RequestBody PersonalRequestDTO request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personalService.registrar(request, authHeader));
    }

    @Operation(summary = "Buscar personal por ID de Keycloak",
            description = "Resuelve el perfil de dominio desde el subject del JWT. Consumido por microservicios que necesiten enriquecer el contexto del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Personal encontrado",
                    content = @Content(schema = @Schema(implementation = PersonalResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "No existe personal vinculado a ese keycloakUserId")
    })
    @GetMapping
    public ResponseEntity<PersonalResponseDTO> buscarPorKeycloakUserId(
            @Parameter(description = "Subject del JWT emitido por Keycloak",
                    example = "550e8400-e29b-41d4-a716-446655440000", required = true)
            @RequestParam String keycloakUserId) {
        return ResponseEntity.ok(personalService.buscarPorKeycloakUserId(keycloakUserId));
    }

    @Operation(summary = "Listar personal con filtros opcionales")
    @GetMapping("/todos")
    public ResponseEntity<List<PersonalResponseDTO>> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) TipoPersonal tipoPersonal,
            @RequestParam(required = false) Boolean estadoActivo) {
        return ResponseEntity.ok(personalService.listar(nombre, tipoPersonal, estadoActivo));
    }

    @Operation(summary = "Listar todos los médicos")
    @GetMapping("/medicos")
    public ResponseEntity<List<PersonalMedicoResponseDTO>> listarMedicos() {
        return ResponseEntity.ok(personalService.listarMedicos());
    }

    @Operation(summary = "Consultar perfil de personal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Personal encontrado",
                    content = @Content(schema = @Schema(implementation = PersonalResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Personal no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PersonalResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(personalService.obtenerPorId(id));
    }

    @Operation(summary = "Verificar habilitación del personal")
    @GetMapping("/{id}/habilitado")
    public ResponseEntity<Boolean> verificarHabilitado(@PathVariable Long id) {
        return ResponseEntity.ok(personalService.verificarHabilitado(id));
    }

    @Operation(summary = "Consultar extensión médica del personal")
    @GetMapping("/{id}/medico-info")
    public ResponseEntity<PersonalMedicoResponseDTO> obtenerMedicoInfo(@PathVariable Long id) {
        return ResponseEntity.ok(personalService.obtenerMedico(id));
    }

    @Operation(summary = "Registrar extensión médica")
    @PostMapping("/{id}/medico-info")
    public ResponseEntity<PersonalMedicoResponseDTO> registrarMedicoInfo(
            @PathVariable Long id,
            @Valid @RequestBody PersonalMedicoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personalService.registrarMedico(id, request));
    }

    @Operation(summary = "Actualizar datos del personal")
    @PatchMapping("/{id}")
    public ResponseEntity<PersonalResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PersonalUpdateRequestDTO request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(personalService.actualizar(id, request, authHeader));
    }

    @Operation(summary = "Habilitar personal")
    @PatchMapping("/{id}/habilitar")
    public ResponseEntity<PersonalResponseDTO> habilitar(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(personalService.cambiarEstado(id, true, null, authHeader));
    }

    @Operation(summary = "Deshabilitar personal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Personal deshabilitado"),
            @ApiResponse(responseCode = "400", description = "El solicitante intenta autodeshabilitarse"),
            @ApiResponse(responseCode = "409", description = "Es el único administrador activo del sistema")
    })
    @PatchMapping("/{id}/deshabilitar")
    public ResponseEntity<PersonalResponseDTO> deshabilitar(
            @PathVariable Long id,
            @RequestParam(required = false) String solicitanteKeycloakUserId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(personalService.cambiarEstado(id, false, solicitanteKeycloakUserId, authHeader));
    }
}
