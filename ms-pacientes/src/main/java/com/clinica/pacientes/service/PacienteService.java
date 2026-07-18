package com.clinica.pacientes.service;

import com.clinica.pacientes.client.AuditoriaClient;
import com.clinica.pacientes.dto.AccionAuditoriaDTO;
import com.clinica.pacientes.dto.AntecedenteClinicoRequestDTO;
import com.clinica.pacientes.dto.AntecedenteClinicoResponseDTO;
import com.clinica.pacientes.dto.PacienteRequestDTO;
import com.clinica.pacientes.dto.PacienteResponseDTO;
import com.clinica.pacientes.dto.PacienteUpdateRequestDTO;
import com.clinica.pacientes.model.AntecedenteClinico;
import com.clinica.pacientes.model.Paciente;
import com.clinica.pacientes.repository.AntecedenteClinicoRepository;
import com.clinica.pacientes.repository.PacienteRepository;
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
public class PacienteService {

    private static final String MODULO = "PACIENTES";

    private final PacienteRepository pacienteRepository;
    private final AntecedenteClinicoRepository antecedenteClinicoRepository;
    private final AuditoriaClient auditoriaClient;

    @Transactional
    public PacienteResponseDTO registrar(PacienteRequestDTO request, String authHeader) {
        Paciente paciente = new Paciente();
        paciente.setDocumentoIdentidad(request.getDocumentoIdentidad());
        paciente.setNombres(request.getNombres());
        paciente.setApellidos(request.getApellidos());
        paciente.setFechaNacimiento(request.getFechaNacimiento());
        paciente.setDireccion(request.getDireccion());
        paciente.setCelular(request.getCelular());
        paciente.setCorreo(request.getCorreo());
        paciente.setSexo(request.getSexo());
        paciente.setGrupoSanguineo(request.getGrupoSanguineo());
        paciente.setEstadoActivo(true);
        Paciente saved = pacienteRepository.save(paciente);

        // Metadatos: solo el ID generado — sin nombre, DNI, dirección ni datos de contacto
        auditarAsync("CREAR_PACIENTE", "Paciente", String.valueOf(saved.getId()),
                "EXITO", null, authHeader, null);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> buscar(String q) {
        return pacienteRepository.buscar(q).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PacienteResponseDTO actualizar(Long id, PacienteUpdateRequestDTO request, String authHeader) {
        Paciente paciente = findById(id);
        if (request.getNombres()         != null) paciente.setNombres(request.getNombres());
        if (request.getApellidos()       != null) paciente.setApellidos(request.getApellidos());
        if (request.getFechaNacimiento() != null) paciente.setFechaNacimiento(request.getFechaNacimiento());
        if (request.getSexo()            != null) paciente.setSexo(request.getSexo());
        if (request.getGrupoSanguineo()  != null) paciente.setGrupoSanguineo(request.getGrupoSanguineo());
        paciente.setDireccion(request.getDireccion());
        paciente.setCelular(request.getCelular());
        paciente.setCorreo(request.getCorreo());
        paciente.setNombreBanco(request.getNombreBanco());
        paciente.setNumeroCuenta(request.getNumeroCuenta());
        Paciente saved = pacienteRepository.save(paciente);

        // Metadatos: solo el ID — sin ningún dato demográfico ni de contacto
        auditarAsync("ACTUALIZAR_PACIENTE", "Paciente", String.valueOf(id),
                "EXITO", null, authHeader, null);

        return toResponse(saved);
    }

    @Transactional
    public PacienteResponseDTO cambiarEstado(Long id, boolean activo, String authHeader) {
        Paciente paciente = findById(id);
        paciente.setEstadoActivo(activo);
        pacienteRepository.save(paciente);

        String accion = activo ? "HABILITAR_PACIENTE" : "DESHABILITAR_PACIENTE";
        auditarAsync(accion, "Paciente", String.valueOf(id), "EXITO", null, authHeader, null);

        return toResponse(paciente);
    }

    @Transactional
    public AntecedenteClinicoResponseDTO registrarAntecedente(Long idPaciente,
                                                               AntecedenteClinicoRequestDTO request,
                                                               String authHeader) {
        Paciente paciente = findById(idPaciente);

        AntecedenteClinico antecedente = new AntecedenteClinico();
        antecedente.setPaciente(paciente);
        antecedente.setDescripcion(request.getDescripcion());
        antecedente.setTipo(request.getTipo());
        AntecedenteClinico saved = antecedenteClinicoRepository.save(antecedente);

        // Metadatos: solo el tipo (ALERGIA, ENFERMEDAD_CRONICA…) — sin la descripción clínica
        auditarAsync("REGISTRAR_ANTECEDENTE", "AntecedenteClinico", String.valueOf(saved.getId()),
                "EXITO", null, authHeader,
                "{\"idPaciente\":" + idPaciente + ",\"tipo\":\"" + request.getTipo() + "\"}");

        return toAntecedenteResponse(saved);
    }

    @Transactional
    public void eliminarAntecedente(Long idPaciente, Long idAntecedente, String authHeader) {
        findById(idPaciente);
        AntecedenteClinico antecedente = antecedenteClinicoRepository.findById(idAntecedente)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Antecedente no encontrado con id: " + idAntecedente));
        if (!antecedente.getPaciente().getId().equals(idPaciente)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El antecedente no pertenece al paciente indicado.");
        }
        antecedenteClinicoRepository.delete(antecedente);

        auditarAsync("ELIMINAR_ANTECEDENTE", "AntecedenteClinico", String.valueOf(idAntecedente),
                "EXITO", null, authHeader,
                "{\"idPaciente\":" + idPaciente + "}");
    }

    @Transactional(readOnly = true)
    public PacienteResponseDTO obtenerPorId(Long id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public void verificarExistencia(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Paciente no encontrado con id: " + id);
        }
    }

    @Transactional(readOnly = true)
    public List<AntecedenteClinicoResponseDTO> obtenerAntecedentes(Long idPaciente) {
        findById(idPaciente);
        return antecedenteClinicoRepository.findByPacienteId(idPaciente).stream()
                .map(this::toAntecedenteResponse)
                .toList();
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

    private Paciente findById(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Paciente no encontrado con id: " + id));
    }

    private PacienteResponseDTO toResponse(Paciente p) {
        PacienteResponseDTO dto = new PacienteResponseDTO();
        dto.setId(p.getId());
        dto.setDocumentoIdentidad(p.getDocumentoIdentidad());
        dto.setNombres(p.getNombres());
        dto.setApellidos(p.getApellidos());
        dto.setFechaNacimiento(p.getFechaNacimiento());
        dto.setDireccion(p.getDireccion());
        dto.setCelular(p.getCelular());
        dto.setCorreo(p.getCorreo());
        dto.setEstadoActivo(p.getEstadoActivo());
        dto.setSexo(p.getSexo());
        dto.setGrupoSanguineo(p.getGrupoSanguineo());
        dto.setNombreBanco(p.getNombreBanco());
        dto.setNumeroCuenta(p.getNumeroCuenta());
        return dto;
    }

    private AntecedenteClinicoResponseDTO toAntecedenteResponse(AntecedenteClinico a) {
        AntecedenteClinicoResponseDTO dto = new AntecedenteClinicoResponseDTO();
        dto.setId(a.getId());
        dto.setIdPaciente(a.getPaciente().getId());
        dto.setDescripcion(a.getDescripcion());
        dto.setTipo(a.getTipo());
        return dto;
    }
}
