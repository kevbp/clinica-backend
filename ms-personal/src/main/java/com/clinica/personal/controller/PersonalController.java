package com.clinica.personal.controller;

import com.clinica.personal.dto.PersonalMedicoRequestDTO;
import com.clinica.personal.dto.PersonalMedicoResponseDTO;
import com.clinica.personal.dto.PersonalRequestDTO;
import com.clinica.personal.dto.PersonalResponseDTO;
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

@Tag(name = "Personal", description = "Gestión del talento humano de la clínica")
@RestController
@RequiredArgsConstructor
public class PersonalController {

    private final PersonalService personalService;

    @Operation(summary = "Registrar personal", description = "Registra un nuevo miembro del personal (cualquier tipo)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Personal registrado",
                    content = @Content(schema = @Schema(implementation = PersonalResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Documento de identidad o keycloakUserId ya registrado")
    })
    @PostMapping("/personal")
    public ResponseEntity<PersonalResponseDTO> registrar(@Valid @RequestBody PersonalRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personalService.registrar(request));
    }

    @Operation(summary = "Consultar perfil de personal",
            description = "Retorna el perfil completo. Si es MEDICO, incluye medicoInfo con especialidad.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Personal encontrado",
                    content = @Content(schema = @Schema(implementation = PersonalResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Personal no encontrado")
    })
    @GetMapping("/personal/{id}")
    public ResponseEntity<PersonalResponseDTO> obtenerPorId(
            @Parameter(description = "ID interno del personal", example = "5", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(personalService.obtenerPorId(id));
    }

    @Operation(summary = "Verificar habilitación del personal",
            description = "Verificación ligera del estado activo. Consumido por ms-citas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de habilitación retornado"),
            @ApiResponse(responseCode = "404", description = "Personal no encontrado")
    })
    @GetMapping("/personal/{id}/habilitado")
    public ResponseEntity<Boolean> verificarHabilitado(
            @Parameter(description = "ID interno del personal", example = "5", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(personalService.verificarHabilitado(id));
    }

    @Operation(summary = "Buscar personal por ID de Keycloak",
            description = "Resuelve el perfil de dominio desde el subject del JWT. Consumido por microservicios que necesiten enriquecer el contexto del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Personal encontrado",
                    content = @Content(schema = @Schema(implementation = PersonalResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "No existe personal vinculado a ese keycloakUserId")
    })
    @GetMapping("/personal")
    public ResponseEntity<PersonalResponseDTO> buscarPorKeycloakUserId(
            @Parameter(description = "Subject del JWT emitido por Keycloak",
                    example = "550e8400-e29b-41d4-a716-446655440000", required = true)
            @RequestParam String keycloakUserId) {
        return ResponseEntity.ok(personalService.buscarPorKeycloakUserId(keycloakUserId));
    }

    @Operation(summary = "Registrar extensión médica",
            description = "Asocia número de colegiatura y especialidad a un Personal existente de tipo MEDICO")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Extensión médica registrada",
                    content = @Content(schema = @Schema(implementation = PersonalMedicoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "El personal no es de tipo MEDICO o datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Personal o especialidad no encontrados"),
            @ApiResponse(responseCode = "409", description = "El personal ya tiene extensión médica registrada")
    })
    @PostMapping("/personal-medico")
    public ResponseEntity<PersonalMedicoResponseDTO> registrarMedico(
            @Valid @RequestBody PersonalMedicoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personalService.registrarMedico(request));
    }
}
