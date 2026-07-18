package com.clinica.auditoria.repository;

import com.clinica.auditoria.model.AccionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AccionUsuarioRepository extends JpaRepository<AccionUsuario, Long> {
    List<AccionUsuario> findByKeycloakUserIdOrderByTimestampDesc(String keycloakUserId);
    List<AccionUsuario> findByModuloOrderByTimestampDesc(String modulo);
    List<AccionUsuario> findByCorrelationIdOrderByTimestampAscIdAsc(String correlationId);
    List<AccionUsuario> findByEntidadTipoAndEntidadIdOrderByTimestampAsc(String entidadTipo, String entidadId);
    List<AccionUsuario> findByAccionOrderByTimestampDesc(String accion);

    @Query("SELECT a FROM AccionUsuario a WHERE " +
           "(:modulo IS NULL OR a.modulo = :modulo) AND " +
           "(:accion IS NULL OR a.accion = :accion) AND " +
           "(:resultado IS NULL OR a.resultado = :resultado) AND " +
           "(:desde IS NULL OR a.timestamp >= :desde) AND " +
           "(:hasta IS NULL OR a.timestamp <= :hasta) " +
           "ORDER BY a.timestamp DESC")
    List<AccionUsuario> filtrar(@Param("modulo") String modulo,
                                @Param("accion") String accion,
                                @Param("resultado") String resultado,
                                @Param("desde") LocalDateTime desde,
                                @Param("hasta") LocalDateTime hasta);
}
