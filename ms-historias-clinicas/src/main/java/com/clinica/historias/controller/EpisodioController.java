package com.clinica.historias.controller;

import com.clinica.historias.dto.EpisodioClinicoResponseDTO;
import com.clinica.historias.dto.EpisodioCompletoResponseDTO;
import com.clinica.historias.service.HistoriasClinicasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Episodios Clínicos",
        description = "EHR inmutable y append-only. Sin precio — precios solo en ms-caja.")
@RestController
@RequestMapping("/episodios")
@RequiredArgsConstructor
public class EpisodioController {

    private final HistoriasClinicasService service;

    @Operation(summary = "Listar episodios de un paciente",
            description = "Retorna vista de lista (sin receta, orden ni adendas)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de episodios")
    })
    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<List<EpisodioClinicoResponseDTO>> listarPorPaciente(
            @Parameter(description = "ID del paciente en ms-pacientes", example = "42", required = true)
            @PathVariable Long idPaciente) {
        return ResponseEntity.ok(service.listarPorPaciente(idPaciente));
    }

    @Operation(summary = "Consultar episodio completo",
            description = "Retorna el episodio con receta, orden de laboratorio y adendas vinculadas. " +
                          "El frontend genera el PDF/vista imprimible a partir de este JSON.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Episodio completo encontrado",
                    content = @Content(schema = @Schema(implementation = EpisodioCompletoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Episodio no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EpisodioCompletoResponseDTO> obtenerCompleto(
            @Parameter(description = "ObjectId MongoDB del episodio",
                    example = "64a1f3b2e4b0c72a9d8e1f0a", required = true)
            @PathVariable String id) {
        return ResponseEntity.ok(service.obtenerCompleto(id));
    }
}
