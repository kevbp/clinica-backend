package com.clinica.atencion.service;

import com.clinica.atencion.client.CitasFeignClient;
import com.clinica.atencion.client.FarmaciaFeignClient;
import com.clinica.atencion.client.LaboratorioFeignClient;
import com.clinica.atencion.client.PacientesFeignClient;
import com.clinica.atencion.client.dto.AntecedenteClinicoDTO;
import com.clinica.atencion.client.dto.CitaMedicaDTO;
import com.clinica.atencion.client.dto.DisponibilidadDTO;
import com.clinica.atencion.client.dto.EstadoCitaUpdateDTO;
import com.clinica.atencion.client.dto.ExamenCatalogoDTO;
import com.clinica.atencion.config.RabbitMQConfig;
import com.clinica.atencion.dto.*;
import com.clinica.atencion.event.*;
import com.clinica.atencion.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtencionMedicaService {

    private static final String KEY_PREFIX = "atencion:borrador:";
    private static final long   TTL_HORAS  = 8;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper        objectMapper;
    private final RabbitTemplate      rabbitTemplate;
    private final CitasFeignClient        citasClient;
    private final PacientesFeignClient    pacientesClient;
    private final FarmaciaFeignClient     farmaciaClient;
    private final LaboratorioFeignClient  laboratorioClient;

    // ---- Iniciar atención ----

    public BorradorResponseDTO iniciar(IniciarAtencionRequestDTO request) {
        String key = key(request.getIdCita());

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un borrador activo para la cita " + request.getIdCita());
        }

        // Validar que la cita está CONFIRMADA
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

    // ---- Agregar diagnóstico ----

    public BorradorResponseDTO agregarDiagnostico(Long idCita, DiagnosticoRequestDTO request) {
        BorradorAtencion borrador = obtenerBorradorOFallar(idCita);

        DiagnosticoBorrador diag = new DiagnosticoBorrador();
        diag.setCodigoCie10(request.getCodigoCie10());
        diag.setDescripcion(request.getDescripcion());
        borrador.setDiagnostico(diag);
        borrador.setObservacionesClinicas(request.getObservacionesClinicas());

        guardarBorrador(key(idCita), borrador);
        return toResponse(borrador);
    }

    // ---- Agregar línea de receta ----

    public AgregarRecetaResponseDTO agregarReceta(Long idCita, AgregarRecetaRequestDTO request) {
        BorradorAtencion borrador = obtenerBorradorOFallar(idCita);

        LineaRecetaBorrador linea = new LineaRecetaBorrador();
        linea.setIdMedicamento(request.getIdMedicamento());
        linea.setCantidad(request.getCantidad());
        linea.setIndicaciones(request.getIndicaciones());
        borrador.getLineasReceta().add(linea);
        guardarBorrador(key(idCita), borrador);

        // Obtener antecedentes/alergias del paciente como advertencia al médico
        List<AntecedenteClinicoDTO> antecedentes = List.of();
        try {
            antecedentes = pacientesClient.obtenerAntecedentes(borrador.getIdPaciente()).getBody();
            if (antecedentes == null) antecedentes = List.of();
        } catch (Exception ex) {
            log.warn("No se pudieron obtener antecedentes para advertencia (idPaciente={}): {}",
                    borrador.getIdPaciente(), ex.getMessage());
        }

        // Obtener disponibilidad de stock como advertencia (nunca descuenta)
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

    public AgregarOrdenResponseDTO agregarOrden(Long idCita, AgregarOrdenRequestDTO request) {
        BorradorAtencion borrador = obtenerBorradorOFallar(idCita);

        // Validar que el examen existe en catálogo (nunca consulta precio)
        ExamenCatalogoDTO examen = laboratorioClient.obtenerExamen(request.getIdExamen()).getBody();

        LineaOrdenBorrador linea = new LineaOrdenBorrador();
        linea.setIdExamen(request.getIdExamen());
        linea.setIndicacionesPreparacion(request.getIndicacionesPreparacion());
        borrador.getLineasOrden().add(linea);
        guardarBorrador(key(idCita), borrador);

        AgregarOrdenResponseDTO response = new AgregarOrdenResponseDTO();
        response.setBorrador(toResponse(borrador));
        response.setDetalleExamen(examen);
        return response;
    }

    // ---- Finalizar atención (orden estricto, no alterar) ----

    public BorradorResponseDTO finalizar(Long idCita) {
        BorradorAtencion borrador = obtenerBorradorOFallar(idCita);

        if (borrador.getDiagnostico() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede finalizar la atención sin un diagnóstico CIE-10.");
        }

        // Paso 1: marcar cita como ATENDIDA (síncrono; si falla, no se continúa)
        citasClient.actualizarEstado(idCita, new EstadoCitaUpdateDTO("ATENDIDA"));
        log.info("Cita id={} marcada como ATENDIDA", idCita);

        // Paso 2: publicar evento EpisodioFinalizado hacia RabbitMQ
        EpisodioFinalizadoEvent evento = buildEvento(borrador);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_ATENCION, RabbitMQConfig.ROUTING_KEY_EPISODIO, evento);
        log.info("Evento EpisodioFinalizado publicado para cita={}", idCita);

        // Paso 3: ms-historias-clinicas persiste (asíncrono, ya disparado por el evento)

        // Paso 4: eliminar borrador de Redis
        redisTemplate.delete(key(idCita));
        log.info("Borrador Redis eliminado para cita={}", idCita);

        return toResponse(borrador);
    }

    // ---- Helpers ----

    private String key(Long idCita) {
        return KEY_PREFIX + idCita;
    }

    private BorradorAtencion obtenerBorradorOFallar(Long idCita) {
        String json = redisTemplate.opsForValue().get(key(idCita));
        if (json == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No existe borrador activo para la cita " + idCita +
                    ". Inicie la atención primero.");
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

    private EpisodioFinalizadoEvent buildEvento(BorradorAtencion b) {
        DiagnosticoEventDTO diag = new DiagnosticoEventDTO(
                b.getDiagnostico().getCodigoCie10(),
                b.getDiagnostico().getDescripcion());

        RecetaEventDTO receta = null;
        if (!b.getLineasReceta().isEmpty()) {
            receta = new RecetaEventDTO(b.getLineasReceta().stream()
                    .map(l -> new LineaRecetaEventDTO(l.getIdMedicamento(), l.getCantidad(), l.getIndicaciones()))
                    .toList());
        }

        OrdenEventDTO orden = null;
        if (!b.getLineasOrden().isEmpty()) {
            orden = new OrdenEventDTO(b.getLineasOrden().stream()
                    .map(l -> new LineaOrdenEventDTO(l.getIdExamen(), l.getIndicacionesPreparacion()))
                    .toList());
        }

        return new EpisodioFinalizadoEvent(
                b.getIdCita(), b.getIdPaciente(), b.getIdPersonalMedico(),
                diag, b.getObservacionesClinicas(), receta, orden);
    }

    private BorradorResponseDTO toResponse(BorradorAtencion b) {
        BorradorResponseDTO dto = new BorradorResponseDTO();
        dto.setIdCita(b.getIdCita());
        dto.setIdPaciente(b.getIdPaciente());
        dto.setIdPersonalMedico(b.getIdPersonalMedico());
        dto.setObservacionesClinicas(b.getObservacionesClinicas());

        if (b.getDiagnostico() != null) {
            DiagnosticoDTO diagDto = new DiagnosticoDTO();
            diagDto.setCodigoCie10(b.getDiagnostico().getCodigoCie10());
            diagDto.setDescripcion(b.getDiagnostico().getDescripcion());
            dto.setDiagnostico(diagDto);
        }

        dto.setLineasReceta(b.getLineasReceta().stream().map(l -> {
            LineaRecetaDTO ld = new LineaRecetaDTO();
            ld.setIdMedicamento(l.getIdMedicamento()); ld.setCantidad(l.getCantidad());
            ld.setIndicaciones(l.getIndicaciones()); return ld;
        }).toList());

        dto.setLineasOrden(b.getLineasOrden().stream().map(l -> {
            LineaOrdenDTO ld = new LineaOrdenDTO();
            ld.setIdExamen(l.getIdExamen());
            ld.setIndicacionesPreparacion(l.getIndicacionesPreparacion()); return ld;
        }).toList());

        return dto;
    }
}
