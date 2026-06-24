package com.clinica.historias.service;

import com.clinica.historias.dto.*;
import com.clinica.historias.event.dto.EpisodioFinalizadoEvent;
import com.clinica.historias.model.*;
import com.clinica.historias.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoriasClinicasService {

    private final MongoTemplate mongoTemplate;
    private final EpisodioClinicoRepository episodioRepository;
    private final RecetaRepository recetaRepository;
    private final OrdenLaboratorioRepository ordenRepository;
    private final AdendaClinicoRepository adendaRepository;

    // --- Procesamiento del evento RabbitMQ ---

    public void procesarEpisodioFinalizado(EpisodioFinalizadoEvent event) {
        EpisodioClinico episodio = new EpisodioClinico();
        episodio.setIdPaciente(event.getIdPaciente());
        episodio.setIdCita(event.getIdCita());
        episodio.setIdPersonalMedico(event.getIdPersonalMedico());
        episodio.setFechaAtencion(LocalDateTime.now());

        Diagnostico diagnostico = new Diagnostico();
        diagnostico.setCodigoCie10(event.getDiagnostico().getCodigoCie10());
        diagnostico.setDescripcion(event.getDiagnostico().getDescripcion());
        episodio.setDiagnostico(diagnostico);
        episodio.setObservacionesClinicas(event.getObservacionesClinicas());

        // Insert garantizado — nunca update (inmutabilidad del EHR)
        EpisodioClinico guardado = mongoTemplate.insert(episodio, "episodios_clinicos");

        if (event.getReceta() != null && event.getReceta().getLineas() != null) {
            Receta receta = new Receta();
            receta.setIdEpisodioClinico(guardado.getIdEpisodio());
            receta.setIdPaciente(event.getIdPaciente());
            receta.setIdPersonalMedico(event.getIdPersonalMedico());
            receta.setLineas(event.getReceta().getLineas().stream().map(l -> {
                LineaReceta lr = new LineaReceta();
                lr.setIdMedicamento(l.getIdMedicamento());
                lr.setCantidad(l.getCantidad());
                lr.setIndicaciones(l.getIndicaciones());
                return lr;
            }).toList());
            recetaRepository.save(receta);
        }

        if (event.getOrdenLaboratorio() != null && event.getOrdenLaboratorio().getLineas() != null) {
            OrdenLaboratorio orden = new OrdenLaboratorio();
            orden.setIdEpisodioClinico(guardado.getIdEpisodio());
            orden.setIdPaciente(event.getIdPaciente());
            orden.setIdPersonalMedico(event.getIdPersonalMedico());
            orden.setLineas(event.getOrdenLaboratorio().getLineas().stream().map(l -> {
                LineaOrden lo = new LineaOrden();
                lo.setIdExamen(l.getIdExamen());
                lo.setIndicacionesPreparacion(l.getIndicacionesPreparacion());
                return lo;
            }).toList());
            ordenRepository.save(orden);
        }
    }

    // --- Consultas HTTP ---

    public List<EpisodioClinicoResponseDTO> listarPorPaciente(Long idPaciente) {
        return episodioRepository.findByIdPaciente(idPaciente).stream()
                .map(this::toEpisodioResponse)
                .toList();
    }

    public EpisodioCompletoResponseDTO obtenerCompleto(String id) {
        EpisodioClinico episodio = episodioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Episodio clínico no encontrado con id: " + id));

        EpisodioCompletoResponseDTO dto = new EpisodioCompletoResponseDTO();
        dto.setIdEpisodio(episodio.getIdEpisodio());
        dto.setIdPaciente(episodio.getIdPaciente());
        dto.setIdCita(episodio.getIdCita());
        dto.setIdPersonalMedico(episodio.getIdPersonalMedico());
        dto.setFechaAtencion(episodio.getFechaAtencion());
        dto.setDiagnostico(toDiagnosticoDTO(episodio.getDiagnostico()));
        dto.setObservacionesClinicas(episodio.getObservacionesClinicas());

        dto.setReceta(recetaRepository.findByIdEpisodioClinico(id)
                .map(this::toRecetaResponse).orElse(null));

        dto.setOrdenLaboratorio(ordenRepository.findByIdEpisodioClinico(id)
                .map(this::toOrdenResponse).orElse(null));

        dto.setAdendas(adendaRepository
                .findByIdEpisodioPadreOrderByFechaCorreccionAsc(id).stream()
                .map(this::toAdendaResponse)
                .toList());

        return dto;
    }

    public List<RecetaResponseDTO> listarRecetasPorPaciente(Long idPaciente) {
        return recetaRepository.findByIdPaciente(idPaciente).stream()
                .map(this::toRecetaResponse)
                .toList();
    }

    public List<OrdenLaboratorioResponseDTO> listarOrdenesPorPaciente(Long idPaciente) {
        return ordenRepository.findByIdPaciente(idPaciente).stream()
                .map(this::toOrdenResponse)
                .toList();
    }

    public RecetaResponseDTO obtenerRecetaPorId(String id) {
        return recetaRepository.findById(id)
                .map(this::toRecetaResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Receta no encontrada con id: " + id));
    }

    public OrdenLaboratorioResponseDTO obtenerOrdenPorId(String id) {
        return ordenRepository.findById(id)
                .map(this::toOrdenResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Orden de laboratorio no encontrada con id: " + id));
    }

    public AdendaClinicoResponseDTO registrarAdenda(AdendaClinicoRequestDTO request) {
        EpisodioClinico episodio = episodioRepository.findById(request.getIdEpisodioPadre())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Episodio clínico no encontrado con id: " + request.getIdEpisodioPadre()));

        if (!episodio.getIdPersonalMedico().equals(request.getIdPersonalMedico())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo el médico autor del episodio puede registrar una adenda sobre él.");
        }

        AdendaClinica adenda = new AdendaClinica();
        adenda.setIdEpisodioPadre(request.getIdEpisodioPadre());
        adenda.setIdPersonalMedico(request.getIdPersonalMedico());
        adenda.setFechaCorreccion(LocalDateTime.now());
        adenda.setTextoRectificacion(request.getTextoRectificacion());

        return toAdendaResponse(adendaRepository.save(adenda));
    }

    // --- Mappers ---

    private EpisodioClinicoResponseDTO toEpisodioResponse(EpisodioClinico e) {
        EpisodioClinicoResponseDTO dto = new EpisodioClinicoResponseDTO();
        dto.setIdEpisodio(e.getIdEpisodio());
        dto.setIdPaciente(e.getIdPaciente());
        dto.setIdCita(e.getIdCita());
        dto.setIdPersonalMedico(e.getIdPersonalMedico());
        dto.setFechaAtencion(e.getFechaAtencion());
        dto.setDiagnostico(toDiagnosticoDTO(e.getDiagnostico()));
        dto.setObservacionesClinicas(e.getObservacionesClinicas());
        return dto;
    }

    private DiagnosticoDTO toDiagnosticoDTO(Diagnostico d) {
        if (d == null) return null;
        DiagnosticoDTO dto = new DiagnosticoDTO();
        dto.setCodigoCie10(d.getCodigoCie10());
        dto.setDescripcion(d.getDescripcion());
        return dto;
    }

    private RecetaResponseDTO toRecetaResponse(Receta r) {
        RecetaResponseDTO dto = new RecetaResponseDTO();
        dto.setIdReceta(r.getIdReceta());
        dto.setIdEpisodioClinico(r.getIdEpisodioClinico());
        dto.setIdPaciente(r.getIdPaciente());
        dto.setIdPersonalMedico(r.getIdPersonalMedico());
        dto.setLineas(r.getLineas() == null ? Collections.emptyList() :
                r.getLineas().stream().map(l -> {
                    LineaRecetaResponseDTO ld = new LineaRecetaResponseDTO();
                    ld.setIdMedicamento(l.getIdMedicamento());
                    ld.setCantidad(l.getCantidad());
                    ld.setIndicaciones(l.getIndicaciones());
                    return ld;
                }).toList());
        return dto;
    }

    private OrdenLaboratorioResponseDTO toOrdenResponse(OrdenLaboratorio o) {
        OrdenLaboratorioResponseDTO dto = new OrdenLaboratorioResponseDTO();
        dto.setIdOrden(o.getIdOrden());
        dto.setIdEpisodioClinico(o.getIdEpisodioClinico());
        dto.setIdPaciente(o.getIdPaciente());
        dto.setIdPersonalMedico(o.getIdPersonalMedico());
        dto.setLineas(o.getLineas() == null ? Collections.emptyList() :
                o.getLineas().stream().map(l -> {
                    LineaOrdenResponseDTO ld = new LineaOrdenResponseDTO();
                    ld.setIdExamen(l.getIdExamen());
                    ld.setIndicacionesPreparacion(l.getIndicacionesPreparacion());
                    return ld;
                }).toList());
        return dto;
    }

    private AdendaClinicoResponseDTO toAdendaResponse(AdendaClinica a) {
        AdendaClinicoResponseDTO dto = new AdendaClinicoResponseDTO();
        dto.setIdAdenda(a.getIdAdenda());
        dto.setIdEpisodioPadre(a.getIdEpisodioPadre());
        dto.setFechaCorreccion(a.getFechaCorreccion());
        dto.setIdPersonalMedico(a.getIdPersonalMedico());
        dto.setTextoRectificacion(a.getTextoRectificacion());
        return dto;
    }
}
