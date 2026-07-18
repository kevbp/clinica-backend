package com.clinica.atencion.service;

import com.clinica.atencion.client.AuditoriaClient;
import com.clinica.atencion.client.CitasFeignClient;
import com.clinica.atencion.client.FarmaciaFeignClient;
import com.clinica.atencion.client.LaboratorioFeignClient;
import com.clinica.atencion.client.PacientesFeignClient;
import com.clinica.atencion.client.PersonalFeignClient;
import com.clinica.atencion.dto.AccionAuditoriaDTO;
import com.clinica.atencion.client.dto.AntecedenteClinicoDTO;
import com.clinica.atencion.client.dto.CitaMedicaDTO;
import com.clinica.atencion.client.dto.DisponibilidadDTO;
import com.clinica.atencion.client.dto.EstadoCitaEnum;
import com.clinica.atencion.client.dto.EstadoCitaUpdateDTO;
import com.clinica.atencion.client.dto.ExamenCatalogoDTO;
import com.clinica.atencion.client.dto.MedicamentoCatalogoDTO;
import com.clinica.atencion.client.dto.PacienteDTO;
import com.clinica.atencion.client.dto.PersonalDTO;
import com.clinica.atencion.config.RabbitMQConfig;
import com.clinica.atencion.dto.*;
import com.clinica.atencion.event.*;
import com.clinica.atencion.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtencionMedicaService {

    private static final String KEY_PREFIX = "atencion:borrador:";
    private static final long   TTL_HORAS  = 8;
    private static final String MODULO     = "ATENCION_MEDICA";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper        objectMapper;
    private final RabbitTemplate      rabbitTemplate;
    private final CitasFeignClient        citasClient;
    private final PacientesFeignClient    pacientesClient;
    private final PersonalFeignClient     personalClient;
    private final FarmaciaFeignClient     farmaciaClient;
    private final LaboratorioFeignClient  laboratorioClient;
    private final AuditoriaClient         auditoriaClient;

    // ---- Iniciar atención ----

    public BorradorResponseDTO iniciar(IniciarAtencionRequestDTO request, String authHeader) {
        String key = key(request.getIdCita());

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un borrador activo para la cita " + request.getIdCita());
        }

        CitaMedicaDTO cita = citasClient.obtenerCita(request.getIdCita()).getBody();
        if (cita == null || !"CONFIRMADA".equals(cita.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se puede iniciar la atención de una cita en estado CONFIRMADA.");
        }

        BorradorAtencion borrador = new BorradorAtencion();
        borrador.setIdCita(request.getIdCita());
        borrador.setIdPaciente(request.getIdPaciente());
        borrador.setIdPersonalMedico(request.getIdPersonalMedico());

        guardarBorrador(key, borrador);
        log.info("Borrador de atención creado para cita={}", request.getIdCita());

        return toResponse(borrador);
    }

    // ---- Autoguardado incremental ----

    public BorradorResponseDTO actualizarBorrador(Long idCita, BorradorAtencion nuevoEstado) {
        obtenerBorradorOFallar(idCita);
        nuevoEstado.setIdCita(idCita);
        guardarBorrador(key(idCita), nuevoEstado);
        return toResponse(nuevoEstado);
    }

    // ---- Recuperar borrador ----

    public BorradorResponseDTO obtenerBorrador(Long idCita) {
        return toResponse(obtenerBorradorOFallar(idCita));
    }

    // ---- Actualizar anamnesis (motivo de consulta + signos vitales) ----

    public BorradorResponseDTO actualizarAnamnesis(Long idCita, ActualizarAnamnesisRequestDTO request) {
        BorradorAtencion borrador = obtenerBorradorOFallar(idCita);

        if (request.getMotivoConsulta() != null) {
            borrador.setMotivoConsulta(request.getMotivoConsulta());
        }

        if (request.getSignosVitales() != null) {
            SignosVitalesDTO sv = request.getSignosVitales();
            SignosVitalesBorrador svb = new SignosVitalesBorrador();
            svb.setPeso(sv.getPeso());
            svb.setTalla(sv.getTalla());
            svb.setPresionArterial(sv.getPresionArterial());
            svb.setFrecuenciaCardiaca(sv.getFrecuenciaCardiaca());
            svb.setTemperatura(sv.getTemperatura());
            svb.setSaturacionOxigeno(sv.getSaturacionOxigeno());
            svb.setFrecuenciaRespiratoria(sv.getFrecuenciaRespiratoria());
            // Calcular IMC si se tienen peso y talla
            if (sv.getPeso() != null && sv.getTalla() != null && sv.getTalla() > 0) {
                double tallaMt = sv.getTalla() / 100.0;
                svb.setImc(Math.round((sv.getPeso() / (tallaMt * tallaMt)) * 100.0) / 100.0);
            } else {
                svb.setImc(sv.getImc());
            }
            borrador.setSignosVitales(svb);
        }

        guardarBorrador(key(idCita), borrador);
        return toResponse(borrador);
    }

    // ---- Agregar diagnóstico ----

    public BorradorResponseDTO agregarDiagnostico(Long idCita, DiagnosticoRequestDTO request, String authHeader) {
        BorradorAtencion borrador = obtenerBorradorOFallar(idCita);

        DiagnosticoBorrador diag = new DiagnosticoBorrador();
        diag.setCodigoCie10(request.getCodigoCie10());
        diag.setDescripcion(request.getDescripcion());
        diag.setTipoDiagnostico(
                request.getTipoDiagnostico() != null ? request.getTipoDiagnostico() : "PRESUNTIVO");
        borrador.setDiagnostico(diag);
        borrador.setObservacionesClinicas(request.getObservacionesClinicas());

        guardarBorrador(key(idCita), borrador);

        return toResponse(borrador);
    }

    // ---- Agregar línea de receta ----

    public AgregarRecetaResponseDTO agregarReceta(Long idCita, AgregarRecetaRequestDTO request, String authHeader) {
        BorradorAtencion borrador = obtenerBorradorOFallar(idCita);

        LineaRecetaBorrador linea = new LineaRecetaBorrador();
        linea.setIdMedicamento(request.getIdMedicamento());
        linea.setDosis(request.getDosis());
        linea.setViaAdministracion(request.getViaAdministracion());
        linea.setFrecuencia(request.getFrecuencia());
        linea.setDuracion(request.getDuracion());
        linea.setCantidadTotal(request.getCantidadTotal());
        linea.setIndicaciones(request.getIndicaciones());
        try {
            MedicamentoCatalogoDTO med = farmaciaClient.obtenerMedicamento(request.getIdMedicamento()).getBody();
            if (med != null) {
                linea.setNombreMedicamento(med.getNombre());
                linea.setPrincipioActivo(med.getPrincipioActivo());
                linea.setPresentacion(med.getPresentacion());
            }
        } catch (Exception ex) {
            log.warn("No se pudo obtener datos del medicamento id={}: {}", request.getIdMedicamento(), ex.getMessage());
        }
        borrador.getLineasReceta().add(linea);
        guardarBorrador(key(idCita), borrador);

        List<AntecedenteClinicoDTO> antecedentes = List.of();
        try {
            antecedentes = pacientesClient.obtenerAntecedentes(borrador.getIdPaciente()).getBody();
            if (antecedentes == null) antecedentes = List.of();
        } catch (Exception ex) {
            log.warn("No se pudieron obtener antecedentes para advertencia (idPaciente={}): {}",
                    borrador.getIdPaciente(), ex.getMessage());
        }

        DisponibilidadDTO disponibilidad = null;
        try {
            disponibilidad = farmaciaClient.obtenerDisponibilidad(request.getIdMedicamento()).getBody();
        } catch (Exception ex) {
            log.warn("No se pudo obtener disponibilidad de medicamento id={}: {}",
                    request.getIdMedicamento(), ex.getMessage());
        }

        AgregarRecetaResponseDTO response = new AgregarRecetaResponseDTO();
        response.setBorrador(toResponse(borrador));
        response.setAdvertenciasAntecedentes(antecedentes);
        response.setDisponibilidadMedicamento(disponibilidad);
        return response;
    }

    // ---- Agregar línea de orden de examen ----

    public AgregarOrdenResponseDTO agregarOrden(Long idCita, AgregarOrdenRequestDTO request, String authHeader) {
        BorradorAtencion borrador = obtenerBorradorOFallar(idCita);

        ExamenCatalogoDTO examen = laboratorioClient.obtenerExamen(request.getIdExamen()).getBody();

        LineaOrdenBorrador linea = new LineaOrdenBorrador();
        linea.setIdExamen(request.getIdExamen());
        linea.setIndicacionesPreparacion(request.getIndicacionesPreparacion());
        if (examen != null) {
            linea.setNombreExamen(examen.getNombre());
            linea.setCategoria(examen.getCategoria());
        }
        borrador.getLineasOrden().add(linea);
        guardarBorrador(key(idCita), borrador);

        AgregarOrdenResponseDTO response = new AgregarOrdenResponseDTO();
        response.setBorrador(toResponse(borrador));
        response.setDetalleExamen(examen);
        return response;
    }

    // ---- Finalizar atención (orden estricto, no alterar) ----

    public BorradorResponseDTO finalizar(Long idCita, String authHeader) {
        BorradorAtencion borrador = obtenerBorradorOFallar(idCita);
        String sesionCid = borrador.getSesionCorrelationId();
        String cidHttp   = MDC.get("correlationId");   // fuente de verdad para auditoría

        if (borrador.getDiagnostico() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede finalizar la atención sin un diagnóstico CIE-10.");
        }

        long tFinalizado = System.currentTimeMillis();

        auditarAsync("FINALIZAR_CONSULTA", "AtencionMedica", String.valueOf(idCita),
                "EXITO", "EpisodioFinalizado", authHeader, cidHttp,
                "{\"idPaciente\":" + borrador.getIdPaciente() +
                ",\"idPersonalMedico\":" + borrador.getIdPersonalMedico() +
                ",\"sesionId\":\"" + sesionCid + "\"}", tFinalizado);

        CitaMedicaDTO citaAtendida = citasClient.actualizarEstado(idCita, new EstadoCitaUpdateDTO(EstadoCitaEnum.ATENDIDA)).getBody();
        log.info("Cita id={} marcada como ATENDIDA", idCita);

        auditarAsync("MARCAR_CITA_ATENDIDA", "CitaMedica", String.valueOf(idCita),
                "EXITO", null, authHeader, cidHttp, null);

        EpisodioFinalizadoEvent evento = buildEvento(borrador);
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_ATENCION, RabbitMQConfig.ROUTING_KEY_EPISODIO, evento, withCorrelationId(cidHttp));
            log.info("Evento EpisodioFinalizado publicado para cita={}", idCita);
            auditarAsync("MSG_ENCOLADO", "EpisodioFinalizado", String.valueOf(idCita),
                    "EXITO", RabbitMQConfig.ROUTING_KEY_EPISODIO, authHeader, cidHttp, null);
        } catch (Exception ex) {
            log.error("Error al publicar EpisodioFinalizado para cita={}: {}", idCita, ex.getMessage());
            auditarAsyncError("MSG_ERROR_ENCOLAR", "EpisodioFinalizado", String.valueOf(idCita),
                    RabbitMQConfig.ROUTING_KEY_EPISODIO, authHeader, cidHttp, ex.getMessage());
        }

        try {
            boolean publicado = publicarNotificacionAtencion(idCita, borrador.getIdPaciente(), citaAtendida, cidHttp);
            if (publicado) {
                auditarAsync("MSG_ENCOLADO_NOTIF", "EpisodioAtendido", String.valueOf(idCita),
                        "EXITO", RabbitMQConfig.ROUTING_KEY_EPISODIO_NOTIFICACION, authHeader, cidHttp, null);
            }
        } catch (Exception ex) {
            log.warn("Error al publicar notificación de atención para cita={}: {}", idCita, ex.getMessage());
            auditarAsyncError("MSG_ERROR_ENCOLAR", "EpisodioAtendido", String.valueOf(idCita),
                    RabbitMQConfig.ROUTING_KEY_EPISODIO_NOTIFICACION, authHeader, cidHttp, ex.getMessage());
        }

        redisTemplate.delete(key(idCita));
        log.info("Borrador Redis eliminado para cita={}", idCita);

        return toResponse(borrador);
    }

    // ---- Auditoría ----

    private void auditarAsync(String accion, String entidadTipo, String entidadId,
                               String resultado, String disparaEvento,
                               String authHeader, String correlationId, String metadatos) {
        auditarAsync(accion, entidadTipo, entidadId, resultado, disparaEvento, authHeader, correlationId, metadatos,
                     System.currentTimeMillis());
    }

    private void auditarAsync(String accion, String entidadTipo, String entidadId,
                               String resultado, String disparaEvento,
                               String authHeader, String correlationId, String metadatos, long timestampMs) {
        CompletableFuture.runAsync(() -> {
            try {
                auditoriaClient.registrar(
                        AccionAuditoriaDTO.builder()
                                .modulo(MODULO)
                                .accion(accion)
                                .entidadTipo(entidadTipo)
                                .entidadId(entidadId)
                                .resultado(resultado)
                                .correlationId(correlationId)
                                .disparaEvento(disparaEvento)
                                .metadatos(metadatos)
                                .timestamp(timestampMs)
                                .build(),
                        authHeader);
            } catch (Exception e) {
                log.warn("ACTION_LOG no registrado [{}/{}]: {}", accion, entidadId, e.getMessage());
            }
        });
    }

    private void auditarAsyncError(String accion, String entidadTipo, String entidadId,
                                   String disparaEvento, String authHeader, String correlationId,
                                   String errorDetalle) {
        long ts = System.currentTimeMillis();
        CompletableFuture.runAsync(() -> {
            try {
                auditoriaClient.registrar(
                        AccionAuditoriaDTO.builder()
                                .modulo(MODULO)
                                .accion(accion)
                                .entidadTipo(entidadTipo)
                                .entidadId(entidadId)
                                .resultado("ERROR")
                                .correlationId(correlationId)
                                .disparaEvento(disparaEvento)
                                .errorDetalle(errorDetalle)
                                .timestamp(ts)
                                .build(),
                        authHeader);
            } catch (Exception e) {
                log.warn("ACTION_LOG no registrado [{}/{}]: {}", accion, entidadId, e.getMessage());
            }
        });
    }

    // ---- Helpers ----

    private String key(Long idCita) { return KEY_PREFIX + idCita; }

    private BorradorAtencion obtenerBorradorOFallar(Long idCita) {
        String json = redisTemplate.opsForValue().get(key(idCita));
        if (json == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No existe borrador activo para la cita " + idCita + ". Inicie la atención primero.");
        }
        try {
            return objectMapper.readValue(json, BorradorAtencion.class);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al deserializar el borrador de Redis.");
        }
    }

    private void guardarBorrador(String key, BorradorAtencion borrador) {
        try {
            String json = objectMapper.writeValueAsString(borrador);
            redisTemplate.opsForValue().set(key, json, TTL_HORAS, TimeUnit.HOURS);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al guardar el borrador en Redis.");
        }
    }

    private boolean publicarNotificacionAtencion(Long idCita, Long idPaciente, CitaMedicaDTO cita, String cid) {
        PacienteDTO paciente = pacientesClient.obtenerPaciente(idPaciente).getBody();
        if (paciente == null || paciente.getCorreo() == null || paciente.getCorreo().isBlank()) {
            log.info("Paciente id={} sin correo, no se publica notificación", idPaciente);
            return false;
        }
        EpisodioAtendidoEvent notificacion = new EpisodioAtendidoEvent(
                idCita,
                paciente.getNombres() + " " + paciente.getApellidos(),
                paciente.getCorreo(),
                cita != null ? cita.getFechaHora() : null,
                true);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_ATENCION, RabbitMQConfig.ROUTING_KEY_EPISODIO_NOTIFICACION, notificacion, withCorrelationId(cid));
        return true;
    }

    private MessagePostProcessor withCorrelationId(String cid) {
        return msg -> {
            if (cid != null) msg.getMessageProperties().setCorrelationId(cid);
            return msg;
        };
    }

    private EpisodioFinalizadoEvent buildEvento(BorradorAtencion b) {
        PacienteSnapshotDTO pacienteSnapshot = buildPacienteSnapshot(b.getIdPaciente());
        MedicoSnapshotDTO   medicoSnapshot   = buildMedicoSnapshot(b.getIdPersonalMedico());

        DiagnosticoEventDTO diag = new DiagnosticoEventDTO(
                b.getDiagnostico().getCodigoCie10(),
                b.getDiagnostico().getDescripcion(),
                b.getDiagnostico().getTipoDiagnostico());

        SignosVitalesEventDTO sv = null;
        if (b.getSignosVitales() != null) {
            SignosVitalesBorrador svb = b.getSignosVitales();
            sv = new SignosVitalesEventDTO();
            sv.setPeso(svb.getPeso());
            sv.setTalla(svb.getTalla());
            sv.setPresionArterial(svb.getPresionArterial());
            sv.setFrecuenciaCardiaca(svb.getFrecuenciaCardiaca());
            sv.setTemperatura(svb.getTemperatura());
            sv.setSaturacionOxigeno(svb.getSaturacionOxigeno());
            sv.setFrecuenciaRespiratoria(svb.getFrecuenciaRespiratoria());
            sv.setImc(svb.getImc());
        }

        RecetaEventDTO receta = null;
        if (!b.getLineasReceta().isEmpty()) {
            receta = new RecetaEventDTO(b.getLineasReceta().stream()
                    .map(l -> new LineaRecetaEventDTO(
                            l.getIdMedicamento(), l.getNombreMedicamento(), l.getPrincipioActivo(),
                            l.getPresentacion(), l.getDosis(), l.getViaAdministracion(),
                            l.getFrecuencia(), l.getDuracion(), l.getCantidadTotal(), l.getIndicaciones()))
                    .toList());
        }

        OrdenEventDTO orden = null;
        if (!b.getLineasOrden().isEmpty()) {
            orden = new OrdenEventDTO(b.getLineasOrden().stream()
                    .map(l -> new LineaOrdenEventDTO(
                            l.getIdExamen(), l.getNombreExamen(), l.getCategoria(), l.getIndicacionesPreparacion()))
                    .toList());
        }

        return new EpisodioFinalizadoEvent(
                b.getIdCita(), b.getIdPaciente(), b.getIdPersonalMedico(),
                pacienteSnapshot, medicoSnapshot,
                b.getMotivoConsulta(), sv,
                diag, b.getObservacionesClinicas(), receta, orden);
    }

    private PacienteSnapshotDTO buildPacienteSnapshot(Long idPaciente) {
        try {
            PacienteDTO p = pacientesClient.obtenerPaciente(idPaciente).getBody();
            if (p == null) return new PacienteSnapshotDTO(idPaciente, null, null, null, null);
            return new PacienteSnapshotDTO(p.getId(), p.getNombres(), p.getApellidos(),
                    p.getDocumentoIdentidad(), p.getFechaNacimiento());
        } catch (Exception ex) {
            log.warn("No se pudo obtener snapshot del paciente id={}: {}", idPaciente, ex.getMessage());
            return new PacienteSnapshotDTO(idPaciente, null, null, null, null);
        }
    }

    private MedicoSnapshotDTO buildMedicoSnapshot(Long idMedico) {
        try {
            PersonalDTO p = personalClient.obtenerPersonal(idMedico).getBody();
            if (p == null) return new MedicoSnapshotDTO(idMedico, null, null, null, null);
            String colegiatura = p.getMedicoInfo() != null ? p.getMedicoInfo().getNumeroColegiatura() : null;
            String especialidad = (p.getMedicoInfo() != null && p.getMedicoInfo().getEspecialidad() != null)
                    ? p.getMedicoInfo().getEspecialidad().getNombre() : null;
            return new MedicoSnapshotDTO(p.getId(), p.getNombres(), p.getApellidos(), colegiatura, especialidad);
        } catch (Exception ex) {
            log.warn("No se pudo obtener snapshot del médico id={}: {}", idMedico, ex.getMessage());
            return new MedicoSnapshotDTO(idMedico, null, null, null, null);
        }
    }

    private BorradorResponseDTO toResponse(BorradorAtencion b) {
        BorradorResponseDTO dto = new BorradorResponseDTO();
        dto.setIdCita(b.getIdCita());
        dto.setIdPaciente(b.getIdPaciente());
        dto.setIdPersonalMedico(b.getIdPersonalMedico());
        dto.setMotivoConsulta(b.getMotivoConsulta());
        dto.setObservacionesClinicas(b.getObservacionesClinicas());

        if (b.getSignosVitales() != null) {
            SignosVitalesBorrador svb = b.getSignosVitales();
            SignosVitalesDTO sv = new SignosVitalesDTO();
            sv.setPeso(svb.getPeso());
            sv.setTalla(svb.getTalla());
            sv.setPresionArterial(svb.getPresionArterial());
            sv.setFrecuenciaCardiaca(svb.getFrecuenciaCardiaca());
            sv.setTemperatura(svb.getTemperatura());
            sv.setSaturacionOxigeno(svb.getSaturacionOxigeno());
            sv.setFrecuenciaRespiratoria(svb.getFrecuenciaRespiratoria());
            sv.setImc(svb.getImc());
            dto.setSignosVitales(sv);
        }

        if (b.getDiagnostico() != null) {
            DiagnosticoDTO diagDto = new DiagnosticoDTO();
            diagDto.setCodigoCie10(b.getDiagnostico().getCodigoCie10());
            diagDto.setDescripcion(b.getDiagnostico().getDescripcion());
            diagDto.setTipoDiagnostico(b.getDiagnostico().getTipoDiagnostico());
            dto.setDiagnostico(diagDto);
        }

        dto.setLineasReceta(b.getLineasReceta().stream().map(l -> {
            LineaRecetaDTO ld = new LineaRecetaDTO();
            ld.setIdMedicamento(l.getIdMedicamento());
            ld.setDosis(l.getDosis());
            ld.setViaAdministracion(l.getViaAdministracion());
            ld.setFrecuencia(l.getFrecuencia());
            ld.setDuracion(l.getDuracion());
            ld.setCantidadTotal(l.getCantidadTotal());
            ld.setIndicaciones(l.getIndicaciones());
            return ld;
        }).toList());

        dto.setLineasOrden(b.getLineasOrden().stream().map(l -> {
            LineaOrdenDTO ld = new LineaOrdenDTO();
            ld.setIdExamen(l.getIdExamen());
            ld.setIndicacionesPreparacion(l.getIndicacionesPreparacion());
            return ld;
        }).toList());

        return dto;
    }
}
