package com.clinica.notificaciones.service;

import com.clinica.notificaciones.dto.ConfiguracionSmtpRequestDTO;
import com.clinica.notificaciones.dto.ConfiguracionSmtpResponseDTO;
import com.clinica.notificaciones.model.ConfiguracionSmtp;
import com.clinica.notificaciones.repository.ConfiguracionSmtpRepository;
import com.clinica.notificaciones.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Properties;

@Service
@RequiredArgsConstructor
public class ConfiguracionSmtpService {

    private static final Long ID_FIJO = 1L;

    private final ConfiguracionSmtpRepository repository;
    private final CryptoUtil cryptoUtil;

    @Transactional(readOnly = true)
    public ConfiguracionSmtpResponseDTO obtener() {
        return toResponse(obtenerEntidadOCrear());
    }

    @Transactional
    public ConfiguracionSmtpResponseDTO actualizar(ConfiguracionSmtpRequestDTO request) {
        ConfiguracionSmtp config = obtenerEntidadOCrear();
        config.setHost(request.getHost());
        config.setPuerto(request.getPuerto());
        config.setUsername(request.getUsername());
        config.setRemitente(request.getRemitente());
        config.setStarttlsEnabled(request.getStarttlsEnabled());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            config.setPasswordCifrada(cryptoUtil.cifrar(request.getPassword()));
        }
        return toResponse(repository.save(config));
    }

    // Construye un JavaMailSender a partir de la configuración guardada en BD.
    // Se reconstruye en cada envío: el volumen de correos de este sistema es bajo
    // y así un cambio de configuración aplica de inmediato, sin caché que invalidar.
    @Transactional(readOnly = true)
    public JavaMailSenderImpl construirMailSender() {
        ConfiguracionSmtp config = obtenerEntidadOCrear();
        if (config.getHost() == null || config.getUsername() == null || config.getPasswordCifrada() == null) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
                    "La configuración SMTP no ha sido completada. Configúrela en Sistema > Configuración.");
        }

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPuerto());
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(cryptoUtil.descifrar(config.getPasswordCifrada()));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(Boolean.TRUE.equals(config.getStarttlsEnabled())));
        return mailSender;
    }

    @Transactional(readOnly = true)
    public String obtenerRemitente() {
        return obtenerEntidadOCrear().getRemitente();
    }

    private ConfiguracionSmtp obtenerEntidadOCrear() {
        return repository.findById(ID_FIJO).orElseGet(() -> {
            ConfiguracionSmtp nueva = new ConfiguracionSmtp();
            nueva.setId(ID_FIJO);
            return repository.save(nueva);
        });
    }

    private ConfiguracionSmtpResponseDTO toResponse(ConfiguracionSmtp c) {
        ConfiguracionSmtpResponseDTO dto = new ConfiguracionSmtpResponseDTO();
        dto.setHost(c.getHost());
        dto.setPuerto(c.getPuerto());
        dto.setUsername(c.getUsername());
        dto.setPasswordConfigurado(c.getPasswordCifrada() != null);
        dto.setRemitente(c.getRemitente());
        dto.setStarttlsEnabled(Boolean.TRUE.equals(c.getStarttlsEnabled()));
        return dto;
    }
}
