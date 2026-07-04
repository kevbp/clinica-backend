package com.clinica.historias.controller;

import com.clinica.historias.dto.EpisodioClinicoResponseDTO;
import com.clinica.historias.dto.HistoriaClinicaResponseDTO;
import com.clinica.historias.model.HistoriaClinica;
import com.clinica.historias.service.HistoriasClinicasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Historias Clínicas")
@RestController
@RequestMapping("/historias")
@RequiredArgsConstructor
public class HistoriaClinicaController {

    private final HistoriasClinicasService service;

    @Operation(summary = "Obtener la historia clínica de un paciente (cabecera)")
    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<HistoriaClinicaResponseDTO> obtenerPorPaciente(@PathVariable Long idPaciente) {
        HistoriaClinica h = service.obtenerHistoriaPorPaciente(idPaciente);
        return ResponseEntity.ok(toResponse(h));
    }

    @Operation(summary = "Listar todos los episodios de una historia clínica")
    @GetMapping("/{idHistoria}/episodios")
    public ResponseEntity<List<EpisodioClinicoResponseDTO>> listarEpisodios(@PathVariable String idHistoria) {
        return ResponseEntity.ok(service.listarEpisodiosPorHistoria(idHistoria));
    }

    private HistoriaClinicaResponseDTO toResponse(HistoriaClinica h) {
        HistoriaClinicaResponseDTO dto = new HistoriaClinicaResponseDTO();
        dto.setId(h.getId());
        dto.setCodigoHistoria(h.getCodigoHistoria());
        dto.setIdPaciente(h.getIdPaciente());
        dto.setFechaCreacion(h.getFechaCreacion());
        dto.setEstado(h.getEstado());
        return dto;
    }
}
