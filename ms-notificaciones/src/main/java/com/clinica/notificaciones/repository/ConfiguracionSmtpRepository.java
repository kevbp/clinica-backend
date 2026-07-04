package com.clinica.notificaciones.repository;

import com.clinica.notificaciones.model.ConfiguracionSmtp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionSmtpRepository extends JpaRepository<ConfiguracionSmtp, Long> {
}
