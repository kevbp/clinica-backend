package com.clinica.citas.service;

import com.clinica.citas.client.CajaFeignClient;
import com.clinica.citas.client.HorariosFeignClient;
import com.clinica.citas.client.PacientesFeignClient;
import com.clinica.citas.client.PersonalFeignClient;
import com.clinica.citas.client.dto.NotaCreditoRequestDTO;
import com.clinica.citas.client.dto.PacienteDTO;
import com.clinica.citas.client.dto.ProgramacionHorarioDTO;
import com.clinica.citas.config.RabbitMQConfig;
import com.clinica.citas.dto.*;
import com.clinica.citas.event.CitaCreadaEvent;
import com.clinica.citas.model.CitaMedica;
import com.clinica.citas.model.EstadoCita;
import com.clinica.citas.repository.CitaMedicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitaMedicaService {

    private final CitaMedicaRepository citaRepository;
    private final PersonalFeignClient personalClient;
    private final PacientesFeignClient pacientesClient;
    private final HorariosFeignClient horariosClient;
    private final CajaFeignClient cajaClient;
    private final RabbitTemplate rabbitTemplate;

    // ---- Consulta individual ----

    @Transactional(readOnly = true)
    public CitaMedicaResponseDTO obtenerPorId(Long id) {
        return toResponse(findById(id));
    }

    // ---- Lazy Evaluation ----

    @Transactional(readOnly = true)
    public List<SlotDisponibleDTO> calcularDisponibilidad(Long idPersonal, LocalDate fecha) {
        // 1. Obtener turnos maestros del médico
        List<ProgramacionHorarioDTO> horarios = horariosClient.getHorariosPorPersonal(idPersonal).getBody();
        if (horarios == null || horarios.isEmpty()) {
            return List.of();
        }

        // 2. Filtrar por el día de la semana de la fecha solicitada
        String diaSemana = mapDayOfWeek(fecha);
        List<ProgramacionHorarioDTO> horariosDelDia = horarios.stream()
                .filter(h -> h.getDiaSemana().equals(diaSemana))
                .toList();

        if (horariosDelDia.isEmpty()) {
            return List.of();
        }

        // 3. Citas ya ocupadas ese día para ese médico
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.plusDays(1).atStartOfDay();
        Set<LocalDateTime> ocupados = citaRepository
                .findByIdPersonalAndFechaHoraBetweenAndEstadoNot(
                        idPersonal, inicioDia, finDia, EstadoCita.CANCELADA)
                .stream()
                .map(CitaMedica::getFechaHora)
                .collect(Collectors.toSet());

        // 4. Generar bloques de 20 minutos disponibles
        LocalDateTime ahora = LocalDateTime.now();
        List<SlotDisponibleDTO> slots = new ArrayList<>();
        for (ProgramacionHorarioDTO horario : horariosDelDia) {
            LocalTime current = horario.getHoraInicio();
            while (!current.plusMinutes(20).isAfter(horario.getHoraFin())) {
                LocalDateTime slotDateTime = fecha.atTime(current);
                if (!ocupados.contains(slotDateTime) && slotDateTime.isAfter(ahora)) {
                    slots.add(new SlotDisponibleDTO(slotDateTime, horario.getConsultorio().getId()));
                }
                current = current.plusMinutes(20);
            }
        }
        return slots;
    }

    // ---- Crear cita ----

    @Transactional
    public CitaMedicaResponseDTO crear(CitaMedicaRequestDTO request) {
        // Validar médico habilitado
        Boolean habilitado = personalClient.verificarHabilitado(request.getIdPersonal()).getBody();
        if (!Boolean.TRUE.equals(habilitado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El médico con id " + request.getIdPersonal() + " no está habilitado.");
        }

        // Obtener perfil del paciente (valida existencia + obtiene correo)
        PacienteDTO paciente = pacientesClient.obtenerPaciente(request.getIdPaciente()).getBody();

        // Obtener horarios del médico y localizar el turno para la fechaHora solicitada
        List<ProgramacionHorarioDTO> horarios =
                horariosClient.getHorariosPorPersonal(request.getIdPersonal()).getBody();

        String diaSemana = mapDayOfWeek(request.getFechaHora().toLocalDate());
        LocalTime horaSlot = request.getFechaHora().toLocalTime();

        ProgramacionHorarioDTO horarioCubierto = (horarios == null ? List.<ProgramacionHorarioDTO>of() : horarios)
                .stream()
                .filter(h -> h.getDiaSemana().equals(diaSemana)
                        && !horaSlot.isBefore(h.getHoraInicio())
                        && horaSlot.plusMinutes(20).compareTo(h.getHoraFin()) <= 0)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La fechaHora solicitada no corresponde a ningún turno del médico."));

        // Validar que el slot no está ya ocupado
        LocalDate fecha = request.getFechaHora().toLocalDate();
        Set<LocalDateTime> ocupados = citaRepository
                .findByIdPersonalAndFechaHoraBetweenAndEstadoNot(
                        request.getIdPersonal(),
                        fecha.atStartOfDay(),
                        fecha.plusDays(1).atStartOfDay(),
                        EstadoCita.CANCELADA)
                .stream()
                .map(CitaMedica::getFechaHora)
                .collect(Collectors.toSet());

        if (ocupados.contains(request.getFechaHora())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El bloque horario solicitado ya está ocupado.");
        }

        // Crear cita
        CitaMedica cita = new CitaMedica();
        cita.setIdPaciente(request.getIdPaciente());
        cita.setIdPersonal(request.getIdPersonal());
        cita.setIdConsultorio(horarioCubierto.getConsultorio().getId());
        cita.setFechaHora(request.getFechaHora());
        cita.setEstado(EstadoCita.PENDIENTE_PAGO);
        cita = citaRepository.save(cita);

        // Publicar evento CitaCreada
        CitaCreadaEvent evento = new CitaCreadaEvent(
                cita.getId(),
                cita.getIdPaciente(),
                cita.getIdPersonal(),
                cita.getIdConsultorio(),
                cita.getFechaHora(),
                paciente != null ? paciente.getContacto() : null
        );
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_CITAS, RabbitMQConfig.ROUTING_KEY_CREADA, evento);
        log.info("Evento CitaCreada publicado para cita id={}", cita.getId());

        return toResponse(cita);
    }

    // ---- Cambio de estado (CONFIRMADA / ATENDIDA) ----

    @Transactional
    public CitaMedicaResponseDTO actualizarEstado(Long id, EstadoUpdateRequestDTO request) {
        CitaMedica cita = findById(id);
        cita.setEstado(request.getEstado());
        return toResponse(citaRepository.save(cita));
    }

    // ---- Cancelación PENDIENTE_PAGO ----

    @Transactional
    public CitaMedicaResponseDTO cancelarPendientePago(Long id) {
        CitaMedica cita = findById(id);
        if (cita.getEstado() != EstadoCita.PENDIENTE_PAGO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se pueden cancelar citas en estado PENDIENTE_PAGO mediante este endpoint.");
        }
        cita.setEstado(EstadoCita.CANCELADA);
        return toResponse(citaRepository.save(cita));
    }

    // ---- Cancelación CONFIRMADA (con ventana de 24h) ----

    @Transactional
    public CitaMedicaResponseDTO cancelarConfirmada(Long id) {
        CitaMedica cita = findById(id);
        validarConfirmadaConAnticipacion(cita, "cancelar");

        // Llamar ms-caja para emitir NotaCredito
        cajaClient.emitirNotaCredito(
                new NotaCreditoRequestDTO(id, "Cancelación de cita con anticipación ≥ 24h"));

        cita.setEstado(EstadoCita.CANCELADA);
        return toResponse(citaRepository.save(cita));
    }

    // ---- Reagendamiento (con ventana de 24h) ----

    @Transactional
    public CitaMedicaResponseDTO reagendar(Long id, ReagendarRequestDTO request) {
        CitaMedica cita = findById(id);
        validarConfirmadaConAnticipacion(cita, "reagendar");

        // Validar que el nuevo slot existe y está disponible
        List<SlotDisponibleDTO> slots = calcularDisponibilidad(
                cita.getIdPersonal(), request.getNuevaFechaHora().toLocalDate());
        boolean slotLibre = slots.stream()
                .anyMatch(s -> s.getFechaHora().equals(request.getNuevaFechaHora()));
        if (!slotLibre) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El nuevo bloque horario no está disponible.");
        }

        cita.setFechaHora(request.getNuevaFechaHora());
        return toResponse(citaRepository.save(cita));
    }

    // ---- Compensación de Saga (invocado por ms-caja) ----

    @Transactional
    public CitaMedicaResponseDTO compensarPagoFallido(Long id) {
        CitaMedica cita = findById(id);
        // Cancela la cita sin emitir NotaCredito ni validar ventana
        // (es una cancelación técnica, no una decisión del paciente)
        cita.setEstado(EstadoCita.CANCELADA);
        log.warn("Saga compensación: cita id={} cancelada por fallo de pago.", id);
        return toResponse(citaRepository.save(cita));
    }

    // ---- Helpers ----

    private CitaMedica findById(Long id) {
        CitaMedica cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cita no encontrada con id: " + id));

        // Auto-cancelación: cita CONFIRMADA con >15 min de retraso → cancelar en este momento
        if (cita.getEstado() == EstadoCita.CONFIRMADA
                && cita.getFechaHora().plusMinutes(15).isBefore(LocalDateTime.now())) {
            log.info("Auto-cancelación por retraso >15 min: cita id={}", id);
            cita.setEstado(EstadoCita.CANCELADA);
            cita = citaRepository.save(cita);
        }
        return cita;
    }

    private void validarConfirmadaConAnticipacion(CitaMedica cita, String accion) {
        if (cita.getEstado() != EstadoCita.CONFIRMADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede " + accion + " una cita en estado CONFIRMADA.");
        }
        if (cita.getFechaHora().minusHours(24).isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se puede " + accion + " con al menos 24 horas de anticipación.");
        }
    }

    private String mapDayOfWeek(LocalDate fecha) {
        return switch (fecha.getDayOfWeek()) {
            case MONDAY    -> "LUNES";
            case TUESDAY   -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY  -> "JUEVES";
            case FRIDAY    -> "VIERNES";
            case SATURDAY  -> "SABADO";
            case SUNDAY    -> "DOMINGO";
        };
    }

    private CitaMedicaResponseDTO toResponse(CitaMedica c) {
        CitaMedicaResponseDTO dto = new CitaMedicaResponseDTO();
        dto.setId(c.getId());
        dto.setIdPaciente(c.getIdPaciente());
        dto.setIdPersonal(c.getIdPersonal());
        dto.setIdConsultorio(c.getIdConsultorio());
        dto.setFechaHora(c.getFechaHora());
        dto.setEstado(c.getEstado());
        return dto;
    }
}
