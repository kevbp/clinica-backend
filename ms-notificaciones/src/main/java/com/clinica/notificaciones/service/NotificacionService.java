package com.clinica.notificaciones.service;

import com.clinica.notificaciones.event.CitaCanceladaEvent;
import com.clinica.notificaciones.event.CitaCreadaEvent;
import com.clinica.notificaciones.event.CitaReagendadaEvent;
import com.clinica.notificaciones.event.EpisodioAtendidoEvent;
import com.clinica.notificaciones.event.PagoConsultaConfirmadoEvent;
import com.clinica.notificaciones.event.ReenviarComprobanteEvent;
import com.clinica.notificaciones.event.ReenviarNotaCreditoEvent;
import com.clinica.notificaciones.event.RetiroSolicitadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private static final String NOMBRE_CENTRO = "Centro Médico Esperanza Sur";
    private static final String SEDE = "Sede Lima Sur";
    private static final String TELEFONO_CONTACTO = "(01) 226 3817";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ConfiguracionSmtpService configuracionSmtpService;

    public void notificarCitaCreada(CitaCreadaEvent evento) {
        if (!evento.isNotificar()) {
            log.info("CitaCreada id={}: notificación desactivada por el usuario, se omite el envío", evento.getIdCita());
            return;
        }
        if (evento.getCorreoPaciente() == null || evento.getCorreoPaciente().isBlank()) {
            log.warn("CitaCreada id={}: sin correo del paciente, notificación omitida", evento.getIdCita());
            return;
        }

        String nombrePaciente = evento.getNombrePaciente() != null ? evento.getNombrePaciente() : "paciente";
        String especialidad = evento.getEspecialidad() != null ? evento.getEspecialidad() : "Por confirmar";
        String nombreMedico = evento.getNombreMedico() != null ? evento.getNombreMedico() : "Por confirmar";

        String asunto = "Su cita médica fue agendada";
        String cuerpo = String.format(
                "Estimado/a %s,%n%n" +
                "Hemos registrado tu cita en el %s.%n%n" +
                "N° de cita: %d%n" +
                "Especialidad: %s%n" +
                "Profesional: %s%n" +
                "Fecha y Hora: %s - %s%n" +
                "Lugar: %s%n" +
                "Si deseas reprogramar o cancelar, por favor contáctate con nosotros al %s%n%n" +
                "IMPORTANTE: Confirme el pago de la consulta en caja para asegurar su turno. " +
                "Sin el pago confirmado, su cita permanecerá en estado pendiente.%n%n" +
                "%s",
                nombrePaciente, NOMBRE_CENTRO, evento.getIdCita(), especialidad, nombreMedico,
                evento.getFechaHora().format(FORMATO_FECHA), evento.getFechaHora().format(FORMATO_HORA),
                SEDE, TELEFONO_CONTACTO, NOMBRE_CENTRO);

        enviar(evento.getCorreoPaciente(), asunto, cuerpo);
        log.info("Notificación CitaCreada enviada a {} (cita={})", evento.getCorreoPaciente(), evento.getIdCita());
    }

    public void notificarCitaCancelada(CitaCanceladaEvent evento) {
        if (!evento.isNotificar()) {
            log.info("CitaCancelada id={}: notificación desactivada, se omite el envío", evento.getIdCita());
            return;
        }
        if (evento.getCorreoPaciente() == null || evento.getCorreoPaciente().isBlank()) {
            log.warn("CitaCancelada id={}: sin correo del paciente, notificación omitida", evento.getIdCita());
            return;
        }

        String nombrePaciente = evento.getNombrePaciente() != null ? evento.getNombrePaciente() : "paciente";
        String especialidad = evento.getEspecialidad() != null ? evento.getEspecialidad() : "Por confirmar";
        String nombreMedico = evento.getNombreMedico() != null ? evento.getNombreMedico() : "Por confirmar";

        String asunto = "Su cita médica fue cancelada";
        String cuerpo = String.format(
                "Estimado/a %s,%n%n" +
                "Le informamos que su cita en el %s ha sido cancelada.%n%n" +
                "N° de cita: %d%n" +
                "Especialidad: %s%n" +
                "Profesional: %s%n" +
                "Fecha y Hora original: %s - %s%n" +
                "Motivo: %s%n%n" +
                "Si desea agendar una nueva cita, contáctenos al %s%n%n" +
                "%s",
                nombrePaciente, NOMBRE_CENTRO, evento.getIdCita(), especialidad, nombreMedico,
                evento.getFechaHora().format(FORMATO_FECHA), evento.getFechaHora().format(FORMATO_HORA),
                evento.getMotivo(), TELEFONO_CONTACTO, NOMBRE_CENTRO);

        // Adjuntar info de NC si fue emitida al cancelar
        if (evento.getNumeroNc() != null) {
            StringBuilder sbNc = new StringBuilder(cuerpo);
            sbNc.append(String.format("%n%n--- NOTA DE CRÉDITO EMITIDA ---%n"));
            sbNc.append(String.format("N° Nota de crédito: %s%n", evento.getNumeroNc()));
            if (evento.getMontoDevolucion() != null)
                sbNc.append(String.format("Monto a devolver: S/ %.2f%n", evento.getMontoDevolucion()));
            if (evento.getMontoRetenido() != null && evento.getMontoRetenido().compareTo(java.math.BigDecimal.ZERO) > 0)
                sbNc.append(String.format("Penalidad retenida: S/ %.2f%n", evento.getMontoRetenido()));
            sbNc.append(String.format("%nAcérquese a nuestras ventanillas o contáctenos para gestionar su devolución."));
            cuerpo = sbNc.toString();
        }

        enviar(evento.getCorreoPaciente(), asunto, cuerpo);
        log.info("Notificación CitaCancelada enviada a {} (cita={})", evento.getCorreoPaciente(), evento.getIdCita());
    }

    public void notificarCitaReagendada(CitaReagendadaEvent evento) {
        if (!evento.isNotificar()) {
            log.info("CitaReagendada id={}: notificación desactivada, se omite el envío", evento.getIdCita());
            return;
        }
        if (evento.getCorreoPaciente() == null || evento.getCorreoPaciente().isBlank()) {
            log.warn("CitaReagendada id={}: sin correo del paciente, notificación omitida", evento.getIdCita());
            return;
        }

        String nombrePaciente = evento.getNombrePaciente() != null ? evento.getNombrePaciente() : "paciente";
        String especialidad = evento.getEspecialidad() != null ? evento.getEspecialidad() : "Por confirmar";
        String nombreMedico = evento.getNombreMedico() != null ? evento.getNombreMedico() : "Por confirmar";

        String asunto = "Su cita médica fue reprogramada";
        String cuerpo = String.format(
                "Estimado/a %s,%n%n" +
                "Le informamos que su cita en el %s ha sido reprogramada.%n%n" +
                "N° de cita: %d%n" +
                "Especialidad: %s%n" +
                "Profesional: %s%n" +
                "Nueva Fecha y Hora: %s - %s%n" +
                "Lugar: %s%n%n" +
                "Si tiene alguna consulta, puede contactarnos al %s%n%n" +
                "%s",
                nombrePaciente, NOMBRE_CENTRO, evento.getIdCita(), especialidad, nombreMedico,
                evento.getNuevaFechaHora().format(FORMATO_FECHA), evento.getNuevaFechaHora().format(FORMATO_HORA),
                SEDE, TELEFONO_CONTACTO, NOMBRE_CENTRO);

        enviar(evento.getCorreoPaciente(), asunto, cuerpo);
        log.info("Notificación CitaReagendada enviada a {} (cita={})", evento.getCorreoPaciente(), evento.getIdCita());
    }

    public void notificarPagoConfirmado(PagoConsultaConfirmadoEvent evento) {
        if (evento.getCorreoPaciente() == null || evento.getCorreoPaciente().isBlank()) {
            log.warn("PagoConsultaConfirmado cita={}: sin correo del paciente, notificación omitida", evento.getIdCita());
            return;
        }

        String nombrePaciente = evento.getNombrePaciente() != null ? evento.getNombrePaciente() : "paciente";
        String numeroBoleta = evento.getNumeroBoleta() != null ? evento.getNumeroBoleta() : "-";
        String especialidad = evento.getEspecialidad() != null ? (" — " + evento.getEspecialidad()) : "";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Estimado/a %s,%n%n", nombrePaciente));
        sb.append(String.format("Su pago ha sido confirmado exitosamente. A continuación el detalle de su boleta:%n%n"));
        sb.append(String.format("Boleta N°: %s%n", numeroBoleta));
        sb.append(String.format("Consulta%s: N° %d%n", especialidad, evento.getIdCita()));
        if (evento.getFechaEmision() != null)
            sb.append(String.format("Fecha de emisión: %s%n", evento.getFechaEmision().format(FORMATO_FECHA)));
        sb.append(String.format("%n"));
        if (evento.getDescuento() != null && evento.getDescuento().compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append(String.format("Precio del servicio: S/ %.2f%n", evento.getMonto()));
            String conceptoDesc = evento.getConceptoDescuento() != null ? evento.getConceptoDescuento() : "Descuento";
            sb.append(String.format("Descuento (%s): -S/ %.2f%n", conceptoDesc, evento.getDescuento()));
        }
        if (evento.getSubtotal() != null) sb.append(String.format("Subtotal (sin IGV): S/ %.2f%n", evento.getSubtotal()));
        if (evento.getIgv() != null) sb.append(String.format("IGV (18%%): S/ %.2f%n", evento.getIgv()));
        if (evento.getMontoTotal() != null) sb.append(String.format("TOTAL PAGADO: S/ %.2f%n", evento.getMontoTotal()));
        sb.append(String.format("%nPuede acercarse a su cita en el horario agendado.%n%n%s", NOMBRE_CENTRO));

        enviar(evento.getCorreoPaciente(), "Pago confirmado — " + numeroBoleta, sb.toString());
        log.info("Notificación PagoConfirmado enviada a {} (cita={})", evento.getCorreoPaciente(), evento.getIdCita());
    }

    public void notificarEpisodioAtendido(EpisodioAtendidoEvent evento) {
        if (!evento.isNotificar()) {
            log.info("EpisodioAtendido cita={}: notificación desactivada, se omite el envío", evento.getIdCita());
            return;
        }
        if (evento.getCorreoPaciente() == null || evento.getCorreoPaciente().isBlank()) {
            log.warn("EpisodioAtendido cita={}: sin correo del paciente, notificación omitida", evento.getIdCita());
            return;
        }

        String nombrePaciente = evento.getNombrePaciente() != null ? evento.getNombrePaciente() : "paciente";
        String fechaHora = evento.getFechaHoraAtencion() != null
                ? evento.getFechaHoraAtencion().format(FORMATO_FECHA) + " - " + evento.getFechaHoraAtencion().format(FORMATO_HORA)
                : "su cita reciente";

        String asunto = "Gracias por su visita";
        String cuerpo = String.format(
                "Estimado/a %s,%n%n" +
                "Le confirmamos que su atención del %s ha finalizado.%n%n" +
                "Gracias por confiar en %s.%n%n" +
                "%s",
                nombrePaciente, fechaHora, NOMBRE_CENTRO, NOMBRE_CENTRO);

        enviar(evento.getCorreoPaciente(), asunto, cuerpo);
        log.info("Notificación EpisodioAtendido enviada a {} (cita={})", evento.getCorreoPaciente(), evento.getIdCita());
    }

    public void notificarReenvioComprobante(ReenviarComprobanteEvent evento) {
        if (evento.getCorreoDestino() == null || evento.getCorreoDestino().isBlank()) {
            log.warn("ReenviarComprobante {}: sin correo destino, omitido", evento.getNumero());
            return;
        }

        String asunto = "Su comprobante de pago — " + evento.getNumero();
        String cuerpo = String.format(
                "Estimado/a cliente,%n%n" +
                "A continuación el detalle de su comprobante de pago:%n%n" +
                "N° de documento: %s%n" +
                "Fecha de emisión: %s%n" +
                "Concepto: Consulta médica%n%n" +
                "Subtotal (sin IGV): S/ %.2f%n" +
                "IGV (18%%):          S/ %.2f%n" +
                "TOTAL:               S/ %.2f%n%n" +
                "Gracias por confiar en el %s.%n%n" +
                "%s",
                evento.getNumero(),
                evento.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                evento.getSubtotal(),
                evento.getIgv(),
                evento.getMontoTotal(),
                NOMBRE_CENTRO,
                NOMBRE_CENTRO);

        enviar(evento.getCorreoDestino(), asunto, cuerpo);
        log.info("Comprobante {} reenviado a {}", evento.getNumero(), evento.getCorreoDestino());
    }

    public void notificarReenvioNotaCredito(ReenviarNotaCreditoEvent evento) {
        if (evento.getCorreoDestino() == null || evento.getCorreoDestino().isBlank()) {
            log.warn("ReenviarNC {}: sin correo destino, omitido", evento.getNumero());
            return;
        }

        String asunto = "Su nota de crédito — " + evento.getNumero();
        String cuerpo = String.format(
                "Estimado/a cliente,%n%n" +
                "A continuación el detalle de su nota de crédito:%n%n" +
                "N° de documento: %s%n" +
                "Fecha de emisión: %s%n" +
                "Tipo: %s%n" +
                "Motivo: %s%n%n" +
                "Monto devuelto: S/ %.2f%n" +
                (evento.getMontoRetenido() != null && evento.getMontoRetenido().compareTo(java.math.BigDecimal.ZERO) > 0
                        ? "Penalidad retenida: S/ " + String.format("%.2f", evento.getMontoRetenido()) + "%n%n"
                        : "%n") +
                "Este saldo está disponible para usar en su próxima consulta o solicitar retiro bancario.%n%n" +
                "Gracias por confiar en el %s.%n%n%s",
                evento.getNumero(),
                evento.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                evento.getTipo(),
                evento.getMotivo(),
                evento.getMonto(),
                NOMBRE_CENTRO, NOMBRE_CENTRO);

        enviar(evento.getCorreoDestino(), asunto, cuerpo);
        log.info("NC {} enviada a {}", evento.getNumero(), evento.getCorreoDestino());
    }

    public void notificarRetiroSolicitado(RetiroSolicitadoEvent evento) {
        if (evento.getCorreoDestino() == null || evento.getCorreoDestino().isBlank()) {
            log.warn("RetiroSolicitado id={}: sin correo destino, omitido", evento.getIdRetiro());
            return;
        }

        String asunto = "Solicitud de retiro bancario registrada";
        String cuerpo = String.format(
                "Estimado/a %s,%n%n" +
                "Hemos recibido su solicitud de retiro bancario.%n%n" +
                "Monto: S/ %.2f%n" +
                "Banco: %s%n" +
                "N° de cuenta: %s%n" +
                "Fecha de solicitud: %s%n%n" +
                "El equipo de caja procesará la transferencia en un plazo de 3 a 5 días hábiles.%n%n" +
                "Si tiene consultas, comuníquese con nosotros al %s.%n%n" +
                "%s",
                evento.getNombreTitular(),
                evento.getMonto(),
                evento.getNombreBanco(),
                evento.getNumeroCuenta(),
                evento.getFechaSolicitud().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                TELEFONO_CONTACTO, NOMBRE_CENTRO);

        enviar(evento.getCorreoDestino(), asunto, cuerpo);
        log.info("Retiro {} notificado a {}", evento.getIdRetiro(), evento.getCorreoDestino());
    }

    // Envío de correo de prueba desde la pantalla de Configuración — a diferencia de enviar(),
    // sí relanza la excepción para que el frontend muestre el motivo exacto del fallo.
    public void enviarPrueba(String destinatario) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(configuracionSmtpService.obtenerRemitente());
        mensaje.setTo(destinatario);
        mensaje.setSubject("Prueba de configuración SMTP — Sistema de Gestión Clínica");
        mensaje.setText("Este es un correo de prueba. Si lo recibió, la configuración SMTP es correcta.");
        configuracionSmtpService.construirMailSender().send(mensaje);
    }

    private void enviar(String destinatario, String asunto, String cuerpo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(configuracionSmtpService.obtenerRemitente());
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            configuracionSmtpService.construirMailSender().send(mensaje);
        } catch (Exception ex) {
            log.error("Error al enviar correo a {}: {}", destinatario, ex.getMessage());
            // No se relanza: un fallo de envío no debe afectar el resto del sistema
        }
    }
}
