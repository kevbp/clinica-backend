package com.clinica.historias.service;

import com.clinica.historias.client.AuditoriaClient;
import com.clinica.historias.dto.*;
import com.clinica.historias.event.dto.EpisodioFinalizadoEvent;
import com.clinica.historias.model.*;
import com.clinica.historias.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoriasClinicasService {

    private static final String MODULO = "HISTORIAS_CLINICAS";

    private final MongoTemplate mongoTemplate;
    private final HistoriaClinicaRepository historiaRepository;
    private final EpisodioClinicoRepository episodioRepository;
    private final RecetaRepository recetaRepository;
    private final OrdenLaboratorioRepository ordenRepository;
    private final AdendaClinicoRepository adendaRepository;
    private final AuditoriaClient auditoriaClient;

    // --- Procesamiento del evento RabbitMQ ---

    public void procesarEpisodioFinalizado(EpisodioFinalizadoEvent event) {
        HistoriaClinica historia = historiaRepository.findByIdPaciente(event.getIdPaciente())
                .orElseGet(() -> crearHistoria(event.getIdPaciente()));

        EpisodioClinico episodio = new EpisodioClinico();
        episodio.setIdHistoriaClinica(historia.getId());
        episodio.setIdPaciente(event.getIdPaciente());
        episodio.setIdCita(event.getIdCita());
        episodio.setIdPersonalMedico(event.getIdPersonalMedico());
        episodio.setFechaAtencion(LocalDateTime.now());
        episodio.setMotivoConsulta(event.getMotivoConsulta());
        episodio.setObservacionesClinicas(event.getObservacionesClinicas());

        if (event.getPaciente() != null) {
            PacienteSnapshot ps = new PacienteSnapshot();
            ps.setId(event.getPaciente().getId());
            ps.setNombres(event.getPaciente().getNombres());
            ps.setApellidos(event.getPaciente().getApellidos());
            ps.setDocumentoIdentidad(event.getPaciente().getDocumentoIdentidad());
            ps.setFechaNacimiento(event.getPaciente().getFechaNacimiento());
            episodio.setPaciente(ps);
        }

        if (event.getMedico() != null) {
            MedicoSnapshot ms = new MedicoSnapshot();
            ms.setId(event.getMedico().getId());
            ms.setNombres(event.getMedico().getNombres());
            ms.setApellidos(event.getMedico().getApellidos());
            ms.setNumeroColegiatura(event.getMedico().getNumeroColegiatura());
            ms.setEspecialidad(event.getMedico().getEspecialidad());
            episodio.setMedico(ms);
        }

        if (event.getSignosVitales() != null) {
            var svEvent = event.getSignosVitales();
            SignosVitales sv = new SignosVitales();
            sv.setPeso(svEvent.getPeso());
            sv.setTalla(svEvent.getTalla());
            sv.setPresionArterial(svEvent.getPresionArterial());
            sv.setFrecuenciaCardiaca(svEvent.getFrecuenciaCardiaca());
            sv.setTemperatura(svEvent.getTemperatura());
            sv.setSaturacionOxigeno(svEvent.getSaturacionOxigeno());
            sv.setFrecuenciaRespiratoria(svEvent.getFrecuenciaRespiratoria());
            sv.setImc(svEvent.getImc());
            episodio.setSignosVitales(sv);
        }

        if (event.getDiagnostico() != null) {
            Diagnostico diagnostico = new Diagnostico();
            diagnostico.setCodigoCie10(event.getDiagnostico().getCodigoCie10());
            diagnostico.setDescripcion(event.getDiagnostico().getDescripcion());
            diagnostico.setTipoDiagnostico(event.getDiagnostico().getTipoDiagnostico());
            episodio.setDiagnostico(diagnostico);
        }

        EpisodioClinico guardado = mongoTemplate.insert(episodio, "episodios_clinicos");

        if (event.getReceta() != null && event.getReceta().getLineas() != null) {
            Receta receta = new Receta();
            receta.setIdEpisodioClinico(guardado.getIdEpisodio());
            receta.setIdPaciente(event.getIdPaciente());
            receta.setIdPersonalMedico(event.getIdPersonalMedico());
            receta.setFechaEmision(guardado.getFechaAtencion());
            receta.setPaciente(guardado.getPaciente());
            receta.setMedico(guardado.getMedico());
            receta.setLineas(event.getReceta().getLineas().stream().map(l -> {
                LineaReceta lr = new LineaReceta();
                lr.setIdMedicamento(l.getIdMedicamento());
                lr.setNombreMedicamento(l.getNombreMedicamento());
                lr.setPrincipioActivo(l.getPrincipioActivo());
                lr.setPresentacion(l.getPresentacion());
                lr.setDosis(l.getDosis());
                lr.setViaAdministracion(l.getViaAdministracion());
                lr.setFrecuencia(l.getFrecuencia());
                lr.setDuracion(l.getDuracion());
                lr.setCantidadTotal(l.getCantidadTotal());
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
            orden.setFechaEmision(guardado.getFechaAtencion());
            orden.setPaciente(guardado.getPaciente());
            orden.setMedico(guardado.getMedico());
            orden.setLineas(event.getOrdenLaboratorio().getLineas().stream().map(l -> {
                LineaOrden lo = new LineaOrden();
                lo.setIdExamen(l.getIdExamen());
                lo.setNombreExamen(l.getNombreExamen());
                lo.setCategoria(l.getCategoria());
                lo.setIndicacionesPreparacion(l.getIndicacionesPreparacion());
                return lo;
            }).toList());
            ordenRepository.save(orden);
        }
    }

    private HistoriaClinica crearHistoria(Long idPaciente) {
        HistoriaClinica h = new HistoriaClinica();
        h.setIdPaciente(idPaciente);
        h.setCodigoHistoria(String.format("HC-%08d", idPaciente));
        h.setFechaCreacion(LocalDateTime.now());
        h.setEstado("ACTIVA");
        return historiaRepository.save(h);
    }

    // --- Consultas ---

    public HistoriaClinica obtenerHistoriaPorPaciente(Long idPaciente) {
        return historiaRepository.findByIdPaciente(idPaciente)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El paciente id=" + idPaciente + " no tiene historia clínica registrada."));
    }

    public List<EpisodioClinicoResponseDTO> listarEpisodiosPorHistoria(String idHistoria) {
        return episodioRepository.findByIdHistoriaClinica(idHistoria).stream()
                .map(this::toEpisodioResponse).toList();
    }

    public List<EpisodioClinicoResponseDTO> listarPorPaciente(Long idPaciente) {
        return episodioRepository.findByIdPaciente(idPaciente).stream()
                .map(this::toEpisodioResponse).toList();
    }

    public EpisodioCompletoResponseDTO obtenerCompleto(String id, String authHeader) {
        EpisodioClinico episodio = episodioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Episodio clínico no encontrado con id: " + id));

        EpisodioCompletoResponseDTO dto = new EpisodioCompletoResponseDTO();
        dto.setIdEpisodio(episodio.getIdEpisodio());
        dto.setIdHistoriaClinica(episodio.getIdHistoriaClinica());
        dto.setIdPaciente(episodio.getIdPaciente());
        dto.setIdCita(episodio.getIdCita());
        dto.setIdPersonalMedico(episodio.getIdPersonalMedico());
        dto.setPaciente(episodio.getPaciente());
        dto.setMedico(episodio.getMedico());
        dto.setFechaAtencion(episodio.getFechaAtencion());
        dto.setMotivoConsulta(episodio.getMotivoConsulta());
        dto.setSignosVitales(episodio.getSignosVitales());
        dto.setDiagnostico(toDiagnosticoDTO(episodio.getDiagnostico()));
        dto.setObservacionesClinicas(episodio.getObservacionesClinicas());

        dto.setReceta(recetaRepository.findByIdEpisodioClinico(id)
                .map(this::toRecetaResponse).orElse(null));
        dto.setOrdenLaboratorio(ordenRepository.findByIdEpisodioClinico(id)
                .map(this::toOrdenResponse).orElse(null));
        dto.setAdendas(adendaRepository
                .findByIdEpisodioPadreOrderByFechaCorreccionAsc(id).stream()
                .map(this::toAdendaResponse).toList());

        return dto;
    }

    public List<RecetaResponseDTO> listarRecetasPorPaciente(Long idPaciente) {
        return recetaRepository.findByIdPaciente(idPaciente).stream()
                .map(this::toRecetaResponse).toList();
    }

    public List<OrdenLaboratorioResponseDTO> listarOrdenesPorPaciente(Long idPaciente) {
        return ordenRepository.findByIdPaciente(idPaciente).stream()
                .map(this::toOrdenResponse).toList();
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el médico autor del episodio puede registrar una adenda sobre él.");
        }

        AdendaClinica adenda = new AdendaClinica();
        adenda.setIdEpisodioPadre(request.getIdEpisodioPadre());
        adenda.setIdPersonalMedico(request.getIdPersonalMedico());
        adenda.setFechaCorreccion(LocalDateTime.now());
        adenda.setTextoRectificacion(request.getTextoRectificacion());

        return toAdendaResponse(adendaRepository.save(adenda));
    }

    // --- Auditoría ---

    private void auditarAsync(String accion, String entidadTipo, String entidadId,
                               String resultado, String disparaEvento,
                               String authHeader, String metadatos) {
        String cid = MDC.get("correlationId");
        CompletableFuture.runAsync(() -> {
            try {
                auditoriaClient.registrar(
                        AccionAuditoriaDTO.builder()
                                .modulo(MODULO)
                                .accion(accion)
                                .entidadTipo(entidadTipo)
                                .entidadId(entidadId)
                                .resultado(resultado)
                                .correlationId(cid)
                                .disparaEvento(disparaEvento)
                                .metadatos(metadatos)
                                .build(),
                        authHeader);
            } catch (Exception e) {
                log.warn("ACTION_LOG no registrado [{}/{}]: {}", accion, entidadId, e.getMessage());
            }
        });
    }

    // --- Mappers ---

    private EpisodioClinicoResponseDTO toEpisodioResponse(EpisodioClinico e) {
        EpisodioClinicoResponseDTO dto = new EpisodioClinicoResponseDTO();
        dto.setIdEpisodio(e.getIdEpisodio());
        dto.setIdHistoriaClinica(e.getIdHistoriaClinica());
        dto.setIdPaciente(e.getIdPaciente());
        dto.setIdCita(e.getIdCita());
        dto.setIdPersonalMedico(e.getIdPersonalMedico());
        dto.setPaciente(e.getPaciente());
        dto.setMedico(e.getMedico());
        dto.setFechaAtencion(e.getFechaAtencion());
        dto.setMotivoConsulta(e.getMotivoConsulta());
        dto.setDiagnostico(toDiagnosticoDTO(e.getDiagnostico()));
        dto.setObservacionesClinicas(e.getObservacionesClinicas());
        return dto;
    }

    private DiagnosticoDTO toDiagnosticoDTO(Diagnostico d) {
        if (d == null) return null;
        DiagnosticoDTO dto = new DiagnosticoDTO();
        dto.setCodigoCie10(d.getCodigoCie10());
        dto.setDescripcion(d.getDescripcion());
        dto.setTipoDiagnostico(d.getTipoDiagnostico());
        return dto;
    }

    private RecetaResponseDTO toRecetaResponse(Receta r) {
        RecetaResponseDTO dto = new RecetaResponseDTO();
        dto.setIdReceta(r.getIdReceta());
        dto.setIdEpisodioClinico(r.getIdEpisodioClinico());
        dto.setIdPaciente(r.getIdPaciente());
        dto.setIdPersonalMedico(r.getIdPersonalMedico());
        dto.setFechaEmision(r.getFechaEmision());
        dto.setPaciente(r.getPaciente());
        dto.setMedico(r.getMedico());
        dto.setLineas(r.getLineas() == null ? Collections.emptyList() :
                r.getLineas().stream().map(l -> {
                    LineaRecetaResponseDTO ld = new LineaRecetaResponseDTO();
                    ld.setIdMedicamento(l.getIdMedicamento());
                    ld.setNombreMedicamento(l.getNombreMedicamento());
                    ld.setPrincipioActivo(l.getPrincipioActivo());
                    ld.setPresentacion(l.getPresentacion());
                    ld.setDosis(l.getDosis());
                    ld.setViaAdministracion(l.getViaAdministracion());
                    ld.setFrecuencia(l.getFrecuencia());
                    ld.setDuracion(l.getDuracion());
                    ld.setCantidadTotal(l.getCantidadTotal());
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
        dto.setFechaEmision(o.getFechaEmision());
        dto.setPaciente(o.getPaciente());
        dto.setMedico(o.getMedico());
        dto.setLineas(o.getLineas() == null ? Collections.emptyList() :
                o.getLineas().stream().map(l -> {
                    LineaOrdenResponseDTO ld = new LineaOrdenResponseDTO();
                    ld.setIdExamen(l.getIdExamen());
                    ld.setNombreExamen(l.getNombreExamen());
                    ld.setCategoria(l.getCategoria());
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
