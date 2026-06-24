package com.clinica.horarios.service;

import com.clinica.horarios.dto.ProgramacionHorarioRequestDTO;
import com.clinica.horarios.dto.ProgramacionHorarioResponseDTO;
import com.clinica.horarios.model.Consultorio;
import com.clinica.horarios.model.ProgramacionHorario;
import com.clinica.horarios.repository.ProgramacionHorarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramacionHorarioService {

    private final ProgramacionHorarioRepository programacionHorarioRepository;
    private final ConsultorioService consultorioService;

    @Transactional
    public ProgramacionHorarioResponseDTO crear(ProgramacionHorarioRequestDTO request) {
        if (request.getHoraFin().isBefore(request.getHoraInicio()) ||
                request.getHoraFin().equals(request.getHoraInicio())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La hora de fin debe ser posterior a la hora de inicio");
        }

        Consultorio consultorio = consultorioService.findById(request.getIdConsultorio());

        List<ProgramacionHorario> conflictos = programacionHorarioRepository.findConflictos(
                consultorio.getId(),
                request.getDiaSemana(),
                request.getIdPersonal(),
                request.getHoraInicio(),
                request.getHoraFin());

        if (!conflictos.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El consultorio " + consultorio.getNumero() + " ya está asignado a otro personal " +
                    "en esa franja horaria del día " + request.getDiaSemana());
        }

        ProgramacionHorario ph = new ProgramacionHorario();
        ph.setIdPersonal(request.getIdPersonal());
        ph.setConsultorio(consultorio);
        ph.setDiaSemana(request.getDiaSemana());
        ph.setHoraInicio(request.getHoraInicio());
        ph.setHoraFin(request.getHoraFin());

        return toResponse(programacionHorarioRepository.save(ph));
    }

    @Transactional(readOnly = true)
    public ProgramacionHorarioResponseDTO obtenerPorId(Long id) {
        return toResponse(programacionHorarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Programación de horario no encontrada con id: " + id)));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!programacionHorarioRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Programación de horario no encontrada con id: " + id);
        }
        programacionHorarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProgramacionHorarioResponseDTO> obtenerPorPersonal(Long idPersonal) {
        return programacionHorarioRepository.findByIdPersonal(idPersonal).stream()
                .map(this::toResponse)
                .toList();
    }

    private ProgramacionHorarioResponseDTO toResponse(ProgramacionHorario ph) {
        ProgramacionHorarioResponseDTO dto = new ProgramacionHorarioResponseDTO();
        dto.setId(ph.getId());
        dto.setIdPersonal(ph.getIdPersonal());
        dto.setConsultorio(consultorioService.toResponse(ph.getConsultorio()));
        dto.setDiaSemana(ph.getDiaSemana());
        dto.setHoraInicio(ph.getHoraInicio());
        dto.setHoraFin(ph.getHoraFin());
        return dto;
    }
}
