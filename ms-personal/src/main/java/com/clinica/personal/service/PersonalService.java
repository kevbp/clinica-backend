package com.clinica.personal.service;

import com.clinica.personal.dto.*;
import com.clinica.personal.model.Personal;
import com.clinica.personal.model.PersonalMedico;
import com.clinica.personal.model.TipoPersonal;
import com.clinica.personal.repository.PersonalMedicoRepository;
import com.clinica.personal.repository.PersonalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PersonalService {

    private final PersonalRepository personalRepository;
    private final PersonalMedicoRepository personalMedicoRepository;
    private final EspecialidadService especialidadService;

    @Transactional
    public PersonalResponseDTO registrar(PersonalRequestDTO request) {
        Personal personal = new Personal();
        personal.setNombres(request.getNombres());
        personal.setApellidos(request.getApellidos());
        personal.setDocumentoIdentidad(request.getDocumentoIdentidad());
        personal.setContacto(request.getContacto());
        personal.setFechaIngreso(request.getFechaIngreso());
        personal.setTipoPersonal(request.getTipoPersonal());
        personal.setKeycloakUserId(request.getKeycloakUserId());
        personal.setEstadoActivo(true);
        return toResponse(personalRepository.save(personal));
    }

    @Transactional(readOnly = true)
    public PersonalResponseDTO obtenerPorId(Long id) {
        Personal personal = findById(id);
        return toResponse(personal);
    }

    @Transactional(readOnly = true)
    public Boolean verificarHabilitado(Long id) {
        Personal personal = findById(id);
        return personal.getEstadoActivo();
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
    public PersonalMedicoResponseDTO registrarMedico(PersonalMedicoRequestDTO request) {
        Personal personal = findById(request.getIdPersonal());

        if (personal.getTipoPersonal() != TipoPersonal.MEDICO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El personal con id " + request.getIdPersonal() + " no es de tipo MEDICO");
        }

        if (personalMedicoRepository.findByPersonalId(request.getIdPersonal()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El personal con id " + request.getIdPersonal() + " ya tiene extensión médica registrada");
        }

        PersonalMedico medico = new PersonalMedico();
        medico.setPersonal(personal);
        medico.setNumeroColegiatura(request.getNumeroColegiatura());
        medico.setEspecialidad(especialidadService.obtenerEntidadPorId(request.getIdEspecialidad()));

        return toMedicoResponse(personalMedicoRepository.save(medico));
    }

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
        dto.setContacto(p.getContacto());
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
        dto.setNumeroColegiatura(m.getNumeroColegiatura());
        dto.setEspecialidad(espDto);
        return dto;
    }
}
