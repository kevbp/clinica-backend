package com.clinica.notificaciones.service;

import com.clinica.notificaciones.event.CitaCreadaEvent;
import com.clinica.notificaciones.event.PagoConsultaConfirmadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final JavaMailSender mailSender;

    @Value("${notificaciones.remitente:noreply@clinica.com}")
    private String remitente;

    public void notificarCitaCreada(CitaCreadaEvent evento) {
        if (evento.getCorreoPaciente() == null || evento.getCorreoPaciente().isBlank()) {
            log.warn("CitaCreada id={}: sin correo del paciente, notificación omitida", evento.getIdCita());
            return;
        }

        String asunto = "Su cita médica fue agendada";
        String cuerpo = String.format(
                "Estimado/a paciente,%n%n" +
                "Su cita médica fue agendada para el %s.%n%n" +
                "IMPORTANTE: Confirme el pago de la consulta en caja para asegurar su turno. " +
                "Sin el pago confirmado, su cita permanecerá en estado pendiente.%n%n" +
                "N° de cita: %d%n%n" +
                "Clínica HIS/LIS",
                evento.getFechaHora(), evento.getIdCita());

        enviar(evento.getCorreoPaciente(), asunto, cuerpo);
        log.info("Notificación CitaCreada enviada a {} (cita={})", evento.getCorreoPaciente(), evento.getIdCita());
    }

    public void notificarPagoConfirmado(PagoConsultaConfirmadoEvent evento) {
        if (evento.getCorreoPaciente() == null || evento.getCorreoPaciente().isBlank()) {
            log.warn("PagoConsultaConfirmado cita={}: sin correo del paciente, notificación omitida", evento.getIdCita());
            return;
        }

        String asunto = "Su pago de consulta fue confirmado";
        String cuerpo = String.format(
                "Estimado/a paciente,%n%n" +
                "Su pago de S/ %.2f para la cita N° %d fue confirmado exitosamente.%n%n" +
                "Puede acercarse a su cita en el horario agendado.%n%n" +
                "Clínica HIS/LIS",
                evento.getMonto(), evento.getIdCita());

        enviar(evento.getCorreoPaciente(), asunto, cuerpo);
        log.info("Notificación PagoConfirmado enviada a {} (cita={})", evento.getCorreoPaciente(), evento.getIdCita());
    }

    private void enviar(String destinatario, String asunto, String cuerpo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
        } catch (Exception ex) {
            log.error("Error al enviar correo a {}: {}", destinatario, ex.getMessage());
            // No se relanza: un fallo de envío no debe afectar el resto del sistema
        }
    }
}
