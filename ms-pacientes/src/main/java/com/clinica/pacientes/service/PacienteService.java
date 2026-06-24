package com.clinica.pacientes.service;

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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final AntecedenteClinicoRepository antecedenteClinicoRepository;

    @Transactional
    public PacienteResponseDTO registrar(PacienteRequestDTO request) {
        Paciente paciente = new Paciente();
        paciente.setDocumentoIdentidad(request.getDocumentoIdentidad());
        paciente.setNombres(request.getNombres());
        paciente.setApellidos(request.getApellidos());
        paciente.setDireccion(request.getDireccion());
        paciente.setContacto(request.getContacto());
        return toResponse(pacienteRepository.save(paciente));
    }

    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> buscar(String q) {
        return pacienteRepository.buscar(q).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PacienteResponseDTO actualizar(Long id, PacienteUpdateRequestDTO request) {
        Paciente paciente = findById(id);
        if (request.getNombres()   != null) paciente.setNombres(request.getNombres());
        if (request.getApellidos() != null) paciente.setApellidos(request.getApellidos());
        if (request.getDireccion() != null) paciente.setDireccion(request.getDireccion());
        if (request.getContacto()  != null) paciente.setContacto(request.getContacto());
        return toResponse(pacienteRepository.save(paciente));
    }

    @Transactional
    public void eliminarAntecedente(Long idPaciente, Long idAntecedente) {
        findById(idPaciente);
        AntecedenteClinico antecedente = antecedenteClinicoRepository.findById(idAntecedente)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Antecedente no encontrado con id: " + idAntecedente));
        if (!antecedente.getPaciente().getId().equals(idPaciente)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El antecedente no pertenece al paciente indicado.");
        }
        antecedenteClinicoRepository.delete(antecedente);
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

    @Transactional
    public AntecedenteClinicoResponseDTO registrarAntecedente(Long idPaciente,
                                                               AntecedenteClinicoRequestDTO request) {
        Paciente paciente = findById(idPaciente);

        AntecedenteClinico antecedente = new AntecedenteClinico();
        antecedente.setPaciente(paciente);
        antecedente.setDescripcion(request.getDescripcion());
        antecedente.setTipo(request.getTipo());

        return toAntecedenteResponse(antecedenteClinicoRepository.save(antecedente));
    }

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
        dto.setDireccion(p.getDireccion());
        dto.setContacto(p.getContacto());
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
