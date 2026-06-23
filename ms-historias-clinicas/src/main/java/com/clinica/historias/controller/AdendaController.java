package com.clinica.historias.controller;

import com.clinica.historias.dto.AdendaClinicoRequestDTO;
import com.clinica.historias.dto.AdendaClinicoResponseDTO;
import com.clinica.historias.service.HistoriasClinicasService;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "Adendas Clínicas",
        description = "Correcciones append-only sobre episodios. El episodio original nunca se modifica.")
@RestController
@RequestMapping("/adendas")
@RequiredArgsConstructor
public class AdendaController {

    private final HistoriasClinicasService service;

    @Operation(summary = "Registrar adenda clínica",
            description = "Crea un documento de corrección vinculado al episodio original. " +
                          "Solo el médico autor del episodio puede registrar una adenda. " +
                          "La fechaCorreccion es asignada por el servidor.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Adenda registrada",
                    content = @Content(schema = @Schema(implementation = AdendaClinicoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "El médico no es el autor del episodio"),
            @ApiResponse(responseCode = "404", description = "Episodio clínico no encontrado")
    })
    @PostMapping
    public ResponseEntity<AdendaClinicoResponseDTO> registrar(
            @Valid @RequestBody AdendaClinicoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarAdenda(request));
    }
}
