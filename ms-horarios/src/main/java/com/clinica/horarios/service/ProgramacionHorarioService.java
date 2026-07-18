package com.clinica.horarios.service;

import com.clinica.horarios.client.AuditoriaClient;
import com.clinica.horarios.dto.AccionAuditoriaDTO;
import com.clinica.horarios.dto.ProgramacionHorarioBatchRequestDTO;
import com.clinica.horarios.dto.ProgramacionHorarioBatchResponseDTO;
import com.clinica.horarios.dto.ProgramacionHorarioRequestDTO;
import com.clinica.horarios.dto.ProgramacionHorarioResponseDTO;
import com.clinica.horarios.dto.ProgramacionHorarioUpdateRequestDTO;
import com.clinica.horarios.model.Consultorio;
import com.clinica.horarios.model.ProgramacionHorario;
import com.clinica.horarios.repository.ProgramacionHorarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgramacionHorarioService {

    private static final String MODULO = "HORARIOS";

    private final ProgramacionHorarioRepository programacionHorarioRepository;
    private final ConsultorioService consultorioService;
    private final AuditoriaClient auditoriaClient;

    @Transactional
    public ProgramacionHorarioResponseDTO crear(ProgramacionHorarioRequestDTO request, String authHeader) {
        validarRangoHorario(request.getHoraInicio(), request.getHoraFin());
        validarFechaNoPasada(request.getFecha());

        Consultorio consultorio = consultorioService.findById(request.getIdConsultorio());
        validarSinConflictos(consultorio, request.getFecha(), request.getIdPersonal(),
                request.getHoraInicio(), request.getHoraFin(), null);

        ProgramacionHorario ph = new ProgramacionHorario();
        ph.setIdPersonal(request.getIdPersonal());
        ph.setConsultorio(consultorio);
        ph.setFecha(request.getFecha());
        ph.setHoraInicio(request.getHoraInicio());
        ph.setHoraFin(request.getHoraFin());
        ProgramacionHorario saved = programacionHorarioRepository.save(ph);

        auditarAsync("CREAR_TURNO", "ProgramacionHorario", String.valueOf(saved.getId()),
                "EXITO", authHeader,
                "{\"idPersonal\":" + saved.getIdPersonal() +
                ",\"idConsultorio\":" + consultorio.getId() +
                ",\"fecha\":\"" + saved.getFecha() + "\"}");

        return toResponse(saved);
    }

    @Transactional
    public ProgramacionHorarioBatchResponseDTO crearBatch(ProgramacionHorarioBatchRequestDTO request,
                                                           String authHeader) {
        validarRangoHorario(request.getHoraInicio(), request.getHoraFin());
        Consultorio consultorio = consultorioService.findById(request.getIdConsultorio());

        List<ProgramacionHorario> creados = new ArrayList<>();
        for (LocalDate fecha : request.getFechas()) {
            validarFechaNoPasada(fecha);
            validarSinConflictos(consultorio, fecha, request.getIdPersonal(),
                    request.getHoraInicio(), request.getHoraFin(), null);

            ProgramacionHorario ph = new ProgramacionHorario();
            ph.setIdPersonal(request.getIdPersonal());
            ph.setConsultorio(consultorio);
            ph.setFecha(fecha);
            ph.setHoraInicio(request.getHoraInicio());
            ph.setHoraFin(request.getHoraFin());
            creados.add(programacionHorarioRepository.save(ph));
        }

        auditarAsync("CREAR_TURNOS_BATCH", "ProgramacionHorario",
                request.getIdPersonal().toString(),
                "EXITO", authHeader,
                "{\"idPersonal\":" + request.getIdPersonal() +
                ",\"idConsultorio\":" + request.getIdConsultorio() +
                ",\"totalFechas\":" + request.getFechas().size() +
                ",\"fechas\":\"" + request.getFechas() + "\"}");

        ProgramacionHorarioBatchResponseDTO response = new ProgramacionHorarioBatchResponseDTO();
        response.setTotal(request.getFechas().size());
        response.setCreados(creados.size());
        response.setTurnos(creados.stream().map(this::toResponse).toList());
        return response;
    }

    @Transactional(readOnly = true)
    public ProgramacionHorarioResponseDTO obtenerPorId(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public ProgramacionHorarioResponseDTO actualizar(Long id, ProgramacionHorarioUpdateRequestDTO request,
                                                      String authHeader) {
        ProgramacionHorario ph = findById(id);

        if (ph.getFecha().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede modificar un turno cuya fecha ya pasó.");
        }

        Consultorio consultorio = ph.getConsultorio();
        if (request.getIdConsultorio() != null) {
            consultorio = consultorioService.findById(request.getIdConsultorio());
        }
        LocalDate fecha    = request.getFecha()      != null ? request.getFecha()      : ph.getFecha();
        var horaInicio     = request.getHoraInicio() != null ? request.getHoraInicio() : ph.getHoraInicio();
        var horaFin        = request.getHoraFin()    != null ? request.getHoraFin()    : ph.getHoraFin();

        validarRangoHorario(horaInicio, horaFin);
        validarFechaNoPasada(fecha);
        validarSinConflictos(consultorio, fecha, ph.getIdPersonal(), horaInicio, horaFin, ph.getId());

        ph.setConsultorio(consultorio);
        ph.setFecha(fecha);
        ph.setHoraInicio(horaInicio);
        ph.setHoraFin(horaFin);
        ProgramacionHorario saved = programacionHorarioRepository.save(ph);

        auditarAsync("ACTUALIZAR_TURNO", "ProgramacionHorario", String.valueOf(id),
                "EXITO", authHeader,
                "{\"idPersonal\":" + saved.getIdPersonal() + ",\"fecha\":\"" + saved.getFecha() + "\"}");

        return toResponse(saved);
    }

    @Transactional
    public void eliminar(Long id, String authHeader) {
        ProgramacionHorario ph = findById(id);
        if (ph.getFecha().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar un turno cuya fecha ya pasó.");
        }
        programacionHorarioRepository.delete(ph);

        auditarAsync("ELIMINAR_TURNO", "ProgramacionHorario", String.valueOf(id),
                "EXITO", authHeader,
                "{\"idPersonal\":" + ph.getIdPersonal() + ",\"fecha\":\"" + ph.getFecha() + "\"}");
    }

    @Transactional(readOnly = true)
    public List<ProgramacionHorarioResponseDTO> obtenerPorPersonal(Long idPersonal,
                                                                    LocalDate desde, LocalDate hasta) {
        List<ProgramacionHorario> turnos = (desde != null && hasta != null)
                ? programacionHorarioRepository.findByIdPersonalAndFechaBetween(idPersonal, desde, hasta)
                : programacionHorarioRepository.findByIdPersonal(idPersonal);
        return turnos.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProgramacionHorarioResponseDTO> obtenerPorConsultorio(Long idConsultorio) {
        consultorioService.findById(idConsultorio);
        return programacionHorarioRepository.findByConsultorioId(idConsultorio).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProgramacionHorarioResponseDTO> listar() {
        return programacionHorarioRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Auditoría ────────────────────────────────────────────────────────────

    private void auditarAsync(String accion, String entidadTipo, String entidadId,
                               String resultado, String authHeader, String metadatos) {
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
                                .metadatos(metadatos)
                                .build(),
                        authHeader);
            } catch (Exception e) {
                log.warn("ACTION_LOG no registrado [{}/{}]: {}", accion, entidadId, e.getMessage());
            }
        });
    }

    // ── Validaciones ─────────────────────────────────────────────────────────

    private void validarRangoHorario(java.time.LocalTime horaInicio, java.time.LocalTime horaFin) {
        if (horaFin.isBefore(horaInicio) || horaFin.equals(horaInicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La hora de fin debe ser posterior a la hora de inicio");
        }
    }

    private void validarFechaNoPasada(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se pueden crear o reprogramar turnos en fechas pasadas");
        }
    }

    private void validarSinConflictos(Consultorio consultorio, LocalDate fecha, Long idPersonal,
                                       java.time.LocalTime horaInicio, java.time.LocalTime horaFin,
                                       Long idExcluir) {
        List<ProgramacionHorario> conflictos = programacionHorarioRepository.findConflictos(
                consultorio.getId(), fecha, idPersonal, horaInicio, horaFin, idExcluir);
        if (!conflictos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El consultorio " + consultorio.getNumero() + " ya está asignado a otro personal " +
                    "en esa franja horaria el " + fecha);
        }
    }

    // ── Mapeo ────────────────────────────────────────────────────────────────

    private ProgramacionHorario findById(Long id) {
        return programacionHorarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Programación de horario no encontrada con id: " + id));
    }

    private ProgramacionHorarioResponseDTO toResponse(ProgramacionHorario ph) {
        ProgramacionHorarioResponseDTO dto = new ProgramacionHorarioResponseDTO();
        dto.setId(ph.getId());
        dto.setIdPersonal(ph.getIdPersonal());
        dto.setConsultorio(consultorioService.toResponse(ph.getConsultorio()));
        dto.setFecha(ph.getFecha());
        dto.setHoraInicio(ph.getHoraInicio());
        dto.setHoraFin(ph.getHoraFin());
        dto.setEsPasado(ph.getFecha().isBefore(LocalDate.now()));
        return dto;
    }
}
