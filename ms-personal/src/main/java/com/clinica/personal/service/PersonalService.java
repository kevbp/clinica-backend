package com.clinica.personal.service;

import com.clinica.personal.client.AuditoriaClient;
import com.clinica.personal.dto.*;
import com.clinica.personal.model.Personal;
import com.clinica.personal.model.PersonalMedico;
import com.clinica.personal.model.TipoPersonal;
import com.clinica.personal.repository.PersonalMedicoRepository;
import com.clinica.personal.repository.PersonalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalService {

    private static final String MODULO = "PERSONAL";

    private final PersonalRepository personalRepository;
    private final PersonalMedicoRepository personalMedicoRepository;
    private final EspecialidadService especialidadService;
    private final AuditoriaClient auditoriaClient;

    @Transactional
    public PersonalResponseDTO registrar(PersonalRequestDTO request, String authHeader) {
        if (request.getTipoPersonal() == TipoPersonal.MEDICO) {
            if (request.getNumeroColegiatura() == null || request.getNumeroColegiatura().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "numeroColegiatura es requerido para personal de tipo MEDICO");
            }
            if (request.getIdEspecialidad() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "idEspecialidad es requerido para personal de tipo MEDICO");
            }
        }

        Personal personal = new Personal();
        personal.setNombres(request.getNombres());
        personal.setApellidos(request.getApellidos());
        personal.setDocumentoIdentidad(request.getDocumentoIdentidad());
        personal.setCelular(request.getCelular());
        personal.setCorreo(request.getCorreo());
        personal.setFechaIngreso(request.getFechaIngreso());
        personal.setTipoPersonal(request.getTipoPersonal());
        personal.setKeycloakUserId(request.getKeycloakUserId());
        personal.setEstadoActivo(true);
        Personal saved = personalRepository.save(personal);

        if (request.getTipoPersonal() == TipoPersonal.MEDICO) {
            PersonalMedico medico = new PersonalMedico();
            medico.setPersonal(saved);
            medico.setNumeroColegiatura(request.getNumeroColegiatura());
            medico.setEspecialidad(especialidadService.obtenerEntidadPorId(request.getIdEspecialidad()));
            personalMedicoRepository.save(medico);
        }

        auditarAsync("CREAR_PERSONAL", "Personal", String.valueOf(saved.getId()),
                "EXITO", null, authHeader,
                "{\"tipoPersonal\":\"" + saved.getTipoPersonal() + "\"}");

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PersonalResponseDTO obtenerPorId(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public PersonalResponseDTO actualizar(Long id, PersonalUpdateRequestDTO request, String authHeader) {
        Personal personal = findById(id);
        TipoPersonal tipoOriginal = personal.getTipoPersonal();

        if (request.getNombres()            != null) personal.setNombres(request.getNombres());
        if (request.getApellidos()          != null) personal.setApellidos(request.getApellidos());
        if (request.getDocumentoIdentidad() != null) personal.setDocumentoIdentidad(request.getDocumentoIdentidad());
        if (request.getCelular()            != null) personal.setCelular(request.getCelular());
        if (request.getCorreo()             != null) personal.setCorreo(request.getCorreo());
        if (request.getFechaIngreso()       != null) personal.setFechaIngreso(request.getFechaIngreso());

        boolean medicoHandled = false;
        boolean cambioTipo = false;

        if (request.getTipoPersonal() != null) {
            TipoPersonal nuevoTipo = request.getTipoPersonal();

            if (tipoOriginal == TipoPersonal.ADMIN && nuevoTipo != TipoPersonal.ADMIN
                    && Boolean.TRUE.equals(personal.getEstadoActivo())
                    && personalRepository.countByTipoPersonalAndEstadoActivo(TipoPersonal.ADMIN, true) <= 1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No se puede cambiar el tipo: es el único administrador activo del sistema.");
            }

            if (nuevoTipo == TipoPersonal.MEDICO && tipoOriginal != TipoPersonal.MEDICO) {
                if (request.getNumeroColegiatura() == null || request.getNumeroColegiatura().isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "numeroColegiatura es requerido al cambiar el tipo a MEDICO");
                }
                if (request.getIdEspecialidad() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "idEspecialidad es requerido al cambiar el tipo a MEDICO");
                }
                PersonalMedico medico = new PersonalMedico();
                medico.setPersonal(personal);
                medico.setNumeroColegiatura(request.getNumeroColegiatura());
                medico.setEspecialidad(especialidadService.obtenerEntidadPorId(request.getIdEspecialidad()));
                personalMedicoRepository.save(medico);
                medicoHandled = true;

            } else if (nuevoTipo != TipoPersonal.MEDICO && tipoOriginal == TipoPersonal.MEDICO) {
                personalMedicoRepository.findByPersonalId(id).ifPresent(personalMedicoRepository::delete);
                medicoHandled = true;
            }

            personal.setTipoPersonal(nuevoTipo);
            cambioTipo = nuevoTipo != tipoOriginal;
        }

        if (!medicoHandled && personal.getTipoPersonal() == TipoPersonal.MEDICO
                && (request.getNumeroColegiatura() != null || request.getIdEspecialidad() != null)) {
            PersonalMedico medico = personalMedicoRepository.findByPersonalId(id)
                    .orElseGet(() -> {
                        PersonalMedico nuevo = new PersonalMedico();
                        nuevo.setPersonal(personal);
                        return nuevo;
                    });
            if (request.getNumeroColegiatura() != null && !request.getNumeroColegiatura().isBlank()) {
                medico.setNumeroColegiatura(request.getNumeroColegiatura());
            }
            if (request.getIdEspecialidad() != null) {
                medico.setEspecialidad(especialidadService.obtenerEntidadPorId(request.getIdEspecialidad()));
            }
            personalMedicoRepository.save(medico);
        }

        Personal savedPersonal = personalRepository.save(personal);

        // CAMBIAR_TIPO_PERSONAL tiene prioridad sobre ACTUALIZAR_PERSONAL en caso de cambio simultáneo
        String accion = cambioTipo ? "CAMBIAR_TIPO_PERSONAL" : "ACTUALIZAR_PERSONAL";
        String meta = cambioTipo
                ? "{\"tipoAnterior\":\"" + tipoOriginal + "\",\"tipoNuevo\":\"" + savedPersonal.getTipoPersonal() + "\"}"
                : null;
        auditarAsync(accion, "Personal", String.valueOf(id), "EXITO", null, authHeader, meta);

        return toResponse(savedPersonal);
    }

    @Transactional(readOnly = true)
    public List<PersonalMedicoResponseDTO> listarMedicos() {
        return personalMedicoRepository.findAll().stream()
                .map(this::toMedicoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PersonalResponseDTO> listar(String nombre, TipoPersonal tipoPersonal, Boolean estadoActivo) {
        return personalRepository.findAll().stream()
                .filter(p -> nombre == null || nombre.isBlank() ||
                        (p.getNombres() + " " + p.getApellidos()).toLowerCase()
                                .contains(nombre.toLowerCase().trim()))
                .filter(p -> tipoPersonal == null || p.getTipoPersonal() == tipoPersonal)
                .filter(p -> estadoActivo == null || p.getEstadoActivo().equals(estadoActivo))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PersonalMedicoResponseDTO obtenerMedico(Long idPersonal) {
        return toMedicoResponse(personalMedicoRepository.findByPersonalId(idPersonal)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró extensión médica para el personal con id: " + idPersonal)));
    }

    @Transactional
    public PersonalResponseDTO cambiarEstado(Long id, boolean activo,
                                             String solicitanteKeycloakUserId, String authHeader) {
        Personal personal = findById(id);

        if (!activo) {
            if (solicitanteKeycloakUserId != null
                    && solicitanteKeycloakUserId.equals(personal.getKeycloakUserId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No puede deshabilitarse a sí mismo.");
            }
            if (personal.getTipoPersonal() == TipoPersonal.ADMIN
                    && personalRepository.countByTipoPersonalAndEstadoActivo(TipoPersonal.ADMIN, true) <= 1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No se puede deshabilitar: es el único administrador activo del sistema.");
            }
        }

        personal.setEstadoActivo(activo);
        personalRepository.save(personal);

        String accion = activo ? "HABILITAR_PERSONAL" : "DESHABILITAR_PERSONAL";
        auditarAsync(accion, "Personal", String.valueOf(id), "EXITO", null, authHeader, null);

        return toResponse(personal);
    }

    @Transactional(readOnly = true)
    public Boolean verificarHabilitado(Long id) {
        return findById(id).getEstadoActivo();
    }

    @Transactional(readOnly = true)
    public PersonalResponseDTO buscarPorKeycloakUserId(String keycloakUserId) {
        Personal personal = personalRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Personal no encontrado con keycloakUserId: " + keycloakUserId));
        return toResponse(personal);
    }

    @Transactional
    public PersonalMedicoResponseDTO registrarMedico(Long id, PersonalMedicoRequestDTO request) {
        Personal personal = findById(id);

        if (personal.getTipoPersonal() != TipoPersonal.MEDICO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El personal con id " + id + " no es de tipo MEDICO");
        }
        if (personalMedicoRepository.findByPersonalId(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El personal con id " + id + " ya tiene extensión médica registrada");
        }

        PersonalMedico medico = new PersonalMedico();
        medico.setPersonal(personal);
        medico.setNumeroColegiatura(request.getNumeroColegiatura());
        medico.setEspecialidad(especialidadService.obtenerEntidadPorId(request.getIdEspecialidad()));

        return toMedicoResponse(personalMedicoRepository.save(medico));
    }

    // ── Auditoría ────────────────────────────────────────────────────────────

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

    // ── Mapeo ────────────────────────────────────────────────────────────────

    private Personal findById(Long id) {
        return personalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Personal no encontrado con id: " + id));
    }

    private PersonalResponseDTO toResponse(Personal p) {
        PersonalResponseDTO dto = new PersonalResponseDTO();
        dto.setId(p.getId());
        dto.setNombres(p.getNombres());
        dto.setApellidos(p.getApellidos());
        dto.setDocumentoIdentidad(p.getDocumentoIdentidad());
        dto.setCelular(p.getCelular());
        dto.setCorreo(p.getCorreo());
        dto.setFechaIngreso(p.getFechaIngreso());
        dto.setEstadoActivo(p.getEstadoActivo());
        dto.setTipoPersonal(p.getTipoPersonal());
        dto.setKeycloakUserId(p.getKeycloakUserId());

        if (p.getTipoPersonal() == TipoPersonal.MEDICO) {
            personalMedicoRepository.findByPersonalId(p.getId())
                    .ifPresent(m -> dto.setMedicoInfo(toMedicoResponse(m)));
        }
        return dto;
    }

    private PersonalMedicoResponseDTO toMedicoResponse(PersonalMedico m) {
        EspecialidadResponseDTO espDto = new EspecialidadResponseDTO();
        espDto.setId(m.getEspecialidad().getId());
        espDto.setNombre(m.getEspecialidad().getNombre());
        espDto.setDescripcion(m.getEspecialidad().getDescripcion());

        PersonalMedicoResponseDTO dto = new PersonalMedicoResponseDTO();
        dto.setIdPersonal(m.getIdPersonal());
        dto.setNombres(m.getPersonal().getNombres());
        dto.setApellidos(m.getPersonal().getApellidos());
        dto.setNumeroColegiatura(m.getNumeroColegiatura());
        dto.setEspecialidad(espDto);
        return dto;
    }
}
