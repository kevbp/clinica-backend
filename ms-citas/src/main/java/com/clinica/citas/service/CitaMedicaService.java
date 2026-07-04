package com.clinica.citas.service;

import com.clinica.citas.client.CajaFeignClient;
import com.clinica.citas.client.HorariosFeignClient;
import com.clinica.citas.client.PacientesFeignClient;
import com.clinica.citas.client.PersonalFeignClient;
import com.clinica.citas.client.dto.NotaCreditoClientDTO;
import com.clinica.citas.client.dto.NotaCreditoRequestDTO;
import com.clinica.citas.client.dto.PacienteDTO;
import com.clinica.citas.client.dto.PersonalDTO;
import com.clinica.citas.client.dto.ProgramacionHorarioDTO;
import com.clinica.citas.config.RabbitMQConfig;
import com.clinica.citas.dto.*;
import com.clinica.citas.event.CitaCanceladaEvent;
import com.clinica.citas.event.CitaCreadaEvent;
import com.clinica.citas.event.CitaReagendadaEvent;
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
import java.util.Comparator;
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

    @Transactional
    public CitaMedicaResponseDTO obtenerPorId(Long id) {
        return toResponse(findById(id));
    }

    // ---- Listado con filtros ----

    @Transactional(readOnly = true)
    public List<CitaMedicaResponseDTO> listar(Long idPaciente, Long idPersonal,
                                               EstadoCita estado, LocalDate fecha) {
        // findAll() + filtrado en Java: evita el problema de Hibernate 6 al inferir
        // el tipo de un parámetro null para un enum (o LocalDateTime) en JPQL (ver CLAUDE.md regla 12).
        LocalDateTime inicio = fecha != null ? fecha.atStartOfDay() : null;
        LocalDateTime fin    = fecha != null ? fecha.plusDays(1).atStartOfDay() : null;
        return citaRepository.findAll().stream()
                .filter(c -> idPaciente == null || idPaciente.equals(c.getIdPaciente()))
                .filter(c -> idPersonal == null || idPersonal.equals(c.getIdPersonal()))
                .filter(c -> estado == null || estado == c.getEstado())
                .filter(c -> inicio == null || !c.getFechaHora().isBefore(inicio))
                .filter(c -> fin == null || c.getFechaHora().isBefore(fin))
                .sorted(Comparator.comparing(CitaMedica::getFechaHora))
                .map(this::toResponse)
                .toList();
    }

    // ---- Lazy Evaluation ----

    @Transactional(readOnly = true)
    public List<SlotDisponibleDTO> calcularDisponibilidad(Long idPersonal, LocalDate fecha) {
        // 1. Obtener turnos del médico para esa fecha concreta (ms-horarios usa fecha, no día de semana)
        List<ProgramacionHorarioDTO> horariosDelDia =
                horariosClient.getHorariosPorPersonal(idPersonal, fecha, fecha).getBody();
        if (horariosDelDia == null || horariosDelDia.isEmpty()) {
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

        // Obtener nombre del médico y especialidad, para embeber en el evento CitaCreada
        PersonalDTO medico = personalClient.obtenerPersonal(request.getIdPersonal()).getBody();

        // Obtener el turno del médico para esa fecha concreta y localizar el bloque de la fechaHora solicitada
        LocalDate fechaSolicitada = request.getFechaHora().toLocalDate();
        List<ProgramacionHorarioDTO> horariosDelDia =
                horariosClient.getHorariosPorPersonal(request.getIdPersonal(), fechaSolicitada, fechaSolicitada).getBody();

        LocalTime horaSlot = request.getFechaHora().toLocalTime();

        ProgramacionHorarioDTO horarioCubierto = (horariosDelDia == null ? List.<ProgramacionHorarioDTO>of() : horariosDelDia)
                .stream()
                .filter(h -> !horaSlot.isBefore(h.getHoraInicio())
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

        // Validar que el paciente no tenga otra cita activa en el mismo instante
        if (citaRepository.existsByIdPacienteAndFechaHoraAndEstadoNot(
                request.getIdPaciente(), request.getFechaHora(), EstadoCita.CANCELADA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El paciente ya tiene una cita programada en ese mismo horario.");
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
                paciente != null ? paciente.getCorreo() : null,
                request.getNotificarCorreo() == null || request.getNotificarCorreo(),
                paciente != null ? (paciente.getNombres() + " " + paciente.getApellidos()) : null,
                medico != null ? (medico.getNombres() + " " + medico.getApellidos()) : null,
                medico != null && medico.getMedicoInfo() != null && medico.getMedicoInfo().getEspecialidad() != null
                        ? medico.getMedicoInfo().getEspecialidad().getNombre() : null
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
        cita = citaRepository.save(cita);
        publicarCitaCancelada(cita, "Cancelación sin pago previo, sin penalidad.");
        return toResponse(cita);
    }

    // ---- Cancelación CONFIRMADA (política de anticipación con penalidad parcial) ----

    /**
     * Cancela una cita CONFIRMADA aplicando la política de cancelación:
     * - ≥24h de anticipación: nota de crédito por el 100 % del pago (CANCELACION_ANTICIPADA).
     * - <24h de anticipación: nota de crédito por el 70 % del pago; se retiene el 30 % como penalidad (CANCELACION_TARDIA).
     */
    @Transactional
    public CitaMedicaResponseDTO cancelarConfirmada(Long id) {
        CitaMedica cita = findById(id);
        if (cita.getEstado() != EstadoCita.CONFIRMADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede cancelar mediante este endpoint una cita en estado CONFIRMADA.");
        }

        boolean conAnticipacion = !cita.getFechaHora().minusHours(24).isBefore(LocalDateTime.now());
        String tipoNc = conAnticipacion ? "CANCELACION_ANTICIPADA" : "CANCELACION_TARDIA";
        String motivo = conAnticipacion
                ? "Cancelación con anticipación ≥ 24h. Devolución total del pago."
                : "Cancelación con anticipación < 24h. Se retiene el 30 % como penalidad; se devuelve el 70 %.";

        NotaCreditoClientDTO nc = cajaClient.emitirNotaCredito(new NotaCreditoRequestDTO(id, motivo, tipoNc)).getBody();

        cita.setEstado(EstadoCita.CANCELADA);
        cita = citaRepository.save(cita);

        String motuvioEvento = conAnticipacion
                ? "Cancelación con anticipación ≥ 24h. Se emitió nota de crédito por el 100 % del pago."
                : "Cancelación con anticipación < 24h. Se emitió nota de crédito por el 70 % del pago (penalidad del 30 % retenida).";
        publicarCitaCancelada(cita, motuvioEvento, nc);
        return toResponse(cita);
    }

    // ---- Cancelación por parte de la clínica (fuerza mayor) ----

    /**
     * La clínica cancela la cita (ausencia del médico, falla técnica, cierre imprevisto).
     * Siempre emite nota de crédito por el 100 % del pago sin importar la anticipación.
     * Solo aplica a citas CONFIRMADAS (que tienen pago).
     */
    @Transactional
    public CitaMedicaResponseDTO cancelarPorClinica(Long id) {
        CitaMedica cita = findById(id);
        if (cita.getEstado() != EstadoCita.CONFIRMADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede cancelar por parte de la clínica una cita en estado CONFIRMADA.");
        }

        NotaCreditoClientDTO ncClinica = cajaClient.emitirNotaCredito(new NotaCreditoRequestDTO(
                id,
                "Cancelación por parte de la clínica por causas de fuerza mayor. Devolución total del pago.",
                "CANCELACION_POR_CLINICA")).getBody();

        cita.setEstado(EstadoCita.CANCELADA);
        cita = citaRepository.save(cita);
        publicarCitaCancelada(cita,
                "La clínica ha cancelado la cita por causas de fuerza mayor. Se emitió nota de crédito por el 100 % del pago.",
                ncClinica);
        return toResponse(cita);
    }

    // ---- Reagendamiento (CONFIRMADA con ≥24h, o PENDIENTE_PAGO sin restricción de anticipación) ----

    @Transactional
    public CitaMedicaResponseDTO reagendar(Long id, ReagendarRequestDTO request) {
        CitaMedica cita = findById(id);

        if (cita.getEstado() == EstadoCita.CONFIRMADA) {
            if (cita.getFechaHora().minusHours(24).isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Solo se puede reagendar una cita CONFIRMADA con al menos 24 horas de anticipación.");
            }
        } else if (cita.getEstado() != EstadoCita.PENDIENTE_PAGO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede reagendar una cita en estado PENDIENTE_PAGO o CONFIRMADA.");
        }

        // 1. Validar que el nuevo slot cae dentro de un turno del médico (igual que crear())
        LocalDate fechaNueva = request.getNuevaFechaHora().toLocalDate();
        List<ProgramacionHorarioDTO> horariosDelDia =
                horariosClient.getHorariosPorPersonal(cita.getIdPersonal(), fechaNueva, fechaNueva).getBody();
        LocalTime horaSlot = request.getNuevaFechaHora().toLocalTime();
        boolean dentroDeturno = (horariosDelDia == null ? List.<ProgramacionHorarioDTO>of() : horariosDelDia)
                .stream()
                .anyMatch(h -> !horaSlot.isBefore(h.getHoraInicio())
                        && horaSlot.plusMinutes(20).compareTo(h.getHoraFin()) <= 0);
        if (!dentroDeturno) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El nuevo bloque horario no corresponde a ningún turno del médico.");
        }

        // 2. Validar que el slot no está tomado por OTRA cita (excluyendo la actual)
        Set<LocalDateTime> ocupados = citaRepository
                .findByIdPersonalAndFechaHoraBetweenAndEstadoNot(
                        cita.getIdPersonal(),
                        fechaNueva.atStartOfDay(),
                        fechaNueva.plusDays(1).atStartOfDay(),
                        EstadoCita.CANCELADA)
                .stream()
                .filter(c -> !c.getId().equals(id))   // excluir la cita que se está reagendando
                .map(CitaMedica::getFechaHora)
                .collect(Collectors.toSet());
        if (ocupados.contains(request.getNuevaFechaHora())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El nuevo bloque horario ya está ocupado por otra cita.");
        }

        // Validar que el paciente no tenga OTRA cita activa en el nuevo instante
        if (citaRepository.existsByIdPacienteAndFechaHoraAndEstadoNotAndIdNot(
                cita.getIdPaciente(), request.getNuevaFechaHora(), EstadoCita.CANCELADA, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El paciente ya tiene otra cita programada en ese mismo horario.");
        }

        cita.setFechaHora(request.getNuevaFechaHora());
        cita = citaRepository.save(cita);

        // Notificar al paciente del reagendamiento
        PacienteDTO paciente = pacientesClient.obtenerPaciente(cita.getIdPaciente()).getBody();
        PersonalDTO medico = personalClient.obtenerPersonal(cita.getIdPersonal()).getBody();
        CitaReagendadaEvent evento = new CitaReagendadaEvent(
                cita.getId(),
                cita.getIdPaciente(),
                cita.getIdPersonal(),
                cita.getFechaHora(),
                paciente != null ? paciente.getCorreo() : null,
                true,
                paciente != null ? (paciente.getNombres() + " " + paciente.getApellidos()) : null,
                medico != null ? (medico.getNombres() + " " + medico.getApellidos()) : null,
                medico != null && medico.getMedicoInfo() != null && medico.getMedicoInfo().getEspecialidad() != null
                        ? medico.getMedicoInfo().getEspecialidad().getNombre() : null
        );
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_CITAS, RabbitMQConfig.ROUTING_KEY_REAGENDADA, evento);
        log.info("Evento CitaReagendada publicado para cita id={}", cita.getId());

        return toResponse(cita);
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

    // Publica CitaCancelada para que ms-notificaciones avise al paciente. No se publica desde
    // compensarPagoFallido: es una cancelación técnica de saga, no una decisión del paciente.
    private void publicarCitaCancelada(CitaMedica cita, String motivo) {
        publicarCitaCancelada(cita, motivo, null);
    }

    private void publicarCitaCancelada(CitaMedica cita, String motivo, NotaCreditoClientDTO nc) {
        PacienteDTO paciente = pacientesClient.obtenerPaciente(cita.getIdPaciente()).getBody();
        PersonalDTO medico = personalClient.obtenerPersonal(cita.getIdPersonal()).getBody();

        CitaCanceladaEvent evento = new CitaCanceladaEvent(
                cita.getId(),
                cita.getIdPaciente(),
                cita.getFechaHora(),
                paciente != null ? paciente.getCorreo() : null,
                true,
                paciente != null ? (paciente.getNombres() + " " + paciente.getApellidos()) : null,
                medico != null ? (medico.getNombres() + " " + medico.getApellidos()) : null,
                medico != null && medico.getMedicoInfo() != null && medico.getMedicoInfo().getEspecialidad() != null
                        ? medico.getMedicoInfo().getEspecialidad().getNombre() : null,
                motivo,
                nc != null ? nc.getNumero() : null,
                nc != null ? nc.getMonto() : null,
                nc != null ? nc.getMontoRetenido() : null,
                nc != null ? nc.getTipo() : null
        );
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_CITAS, RabbitMQConfig.ROUTING_KEY_CANCELADA, evento);
        log.info("Evento CitaCancelada publicado para cita id={}", cita.getId());
    }

    private CitaMedica findById(Long id) {
        CitaMedica cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cita no encontrada con id: " + id));

        // Auto-cancelación: cita CONFIRMADA con >15 min de retraso → no-show.
        // Se emite NC del 70 % (penalidad del 30 %) si el pago existe (CONFIRMADA siempre tiene pago).
        if (cita.getEstado() == EstadoCita.CONFIRMADA
                && cita.getFechaHora().plusMinutes(15).isBefore(LocalDateTime.now())) {
            log.info("Auto-cancelación por no-show (>15 min): cita id={}", id);
            cita.setEstado(EstadoCita.CANCELADA);
            cita = citaRepository.save(cita);
            // Intentar emitir NC de no-show; si ms-caja no está disponible se loguea y se continúa.
            NotaCreditoClientDTO ncNoShow = null;
            try {
                ncNoShow = cajaClient.emitirNotaCredito(new NotaCreditoRequestDTO(
                        id,
                        "No presentación del paciente (auto-cancelación por retraso > 15 min). Se retiene el 30 % como penalidad; se devuelve el 70 %.",
                        "NO_SHOW")).getBody();
            } catch (Exception e) {
                log.warn("No se pudo emitir nota de crédito de no-show para cita id={}: {}", id, e.getMessage());
            }
            publicarCitaCancelada(cita,
                    "Auto-cancelación por no presentación (más de 15 minutos de retraso). Se emitió nota de crédito por el 70 % del pago (penalidad del 30 % retenida).",
                    ncNoShow);
        }
        return cita;
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
