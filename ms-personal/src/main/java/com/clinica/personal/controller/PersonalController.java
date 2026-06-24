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

    @Operation(summary = "Actualizar datos del personal",
            description = "Actualización parcial. Solo se modifican los campos no nulos enviados en el body.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Personal actualizado",
                    content = @Content(schema = @Schema(implementation = PersonalResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Personal no encontrado"),
            @ApiResponse(responseCode = "409", description = "Documento de identidad ya registrado en otro personal")
    })
    @PatchMapping("/personal/{id}")
    public ResponseEntity<PersonalResponseDTO> actualizar(
            @Parameter(description = "ID interno del personal", example = "5", required = true)
            @PathVariable Long id,
            @RequestBody PersonalUpdateRequestDTO request) {
        return ResponseEntity.ok(personalService.actualizar(id, request));
    }

    @Operation(summary = "Listar todos los médicos",
            description = "Retorna todos los médicos con su colegiatura y especialidad. Útil para el selector de agendamiento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de médicos")
    })
    @GetMapping("/personal-medico")
    public ResponseEntity<List<PersonalMedicoResponseDTO>> listarMedicos() {
        return ResponseEntity.ok(personalService.listarMedicos());
    }

    @Operation(summary = "Listar personal con filtros opcionales",
            description = "Retorna todo el personal. Acepta filtros opcionales: nombre (búsqueda parcial en nombres+apellidos), tipoPersonal y estadoActivo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de personal")
    })
    @GetMapping("/personal/todos")
    public ResponseEntity<List<PersonalResponseDTO>> listar(
            @Parameter(description = "Búsqueda parcial en nombres y apellidos", example = "García")
            @RequestParam(required = false) String nombre,
            @Parameter(description = "Filtrar por tipo: MEDICO, ENFERMERO, ADMINISTRATIVO, etc.")
            @RequestParam(required = false) TipoPersonal tipoPersonal,
            @Parameter(description = "Filtrar por estado activo")
            @RequestParam(required = false) Boolean estadoActivo) {
        return ResponseEntity.ok(personalService.listar(nombre, tipoPersonal, estadoActivo));
    }

    @Operation(summary = "Consultar extensión médica",
            description = "Retorna la colegiatura y especialidad del médico. 404 si el personal no tiene extensión médica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Extensión médica encontrada",
                    content = @Content(schema = @Schema(implementation = PersonalMedicoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "El personal no tiene extensión médica registrada")
    })
    @GetMapping("/personal-medico/{id}")
    public ResponseEntity<PersonalMedicoResponseDTO> obtenerMedico(
            @Parameter(description = "ID interno del personal", example = "5", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(personalService.obtenerMedico(id));
    }

    @Operation(summary = "Habilitar personal",
            description = "Activa el personal para que pueda recibir citas. Idempotente si ya estaba activo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Personal habilitado",
                    content = @Content(schema = @Schema(implementation = PersonalResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Personal no encontrado")
    })
    @PatchMapping("/personal/{id}/habilitar")
    public ResponseEntity<PersonalResponseDTO> habilitar(
            @Parameter(description = "ID interno del personal", example = "5", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(personalService.cambiarEstado(id, true));
    }

    @Operation(summary = "Deshabilitar personal",
            description = "Desactiva el personal impidiendo nuevos agendamientos. Idempotente si ya estaba inactivo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Personal deshabilitado",
                    content = @Content(schema = @Schema(implementation = PersonalResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Personal no encontrado")
    })
    @PatchMapping("/personal/{id}/deshabilitar")
    public ResponseEntity<PersonalResponseDTO> deshabilitar(
            @Parameter(description = "ID interno del personal", example = "5", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(personalService.cambiarEstado(id, false));
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
