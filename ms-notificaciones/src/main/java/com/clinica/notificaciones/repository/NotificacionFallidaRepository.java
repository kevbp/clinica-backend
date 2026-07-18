package com.clinica.notificaciones.repository;

import com.clinica.notificaciones.model.NotificacionFallida;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionFallidaRepository extends JpaRepository<NotificacionFallida, Long> {
    java.util.List<NotificacionFallida> findByStatusOrderByFechaFalloDesc(String status);
}
