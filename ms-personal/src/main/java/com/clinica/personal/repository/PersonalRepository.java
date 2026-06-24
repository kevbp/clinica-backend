package com.clinica.personal.repository;

import com.clinica.personal.model.Personal;
import com.clinica.personal.model.TipoPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonalRepository extends JpaRepository<Personal, Long> {

    Optional<Personal> findByKeycloakUserId(String keycloakUserId);

    @Query("""
            SELECT p FROM Personal p
            WHERE (:nombre IS NULL OR LOWER(CONCAT(p.nombres, ' ', p.apellidos)) LIKE LOWER(CONCAT('%', :nombre, '%')))
              AND (:tipoPersonal IS NULL OR p.tipoPersonal = :tipoPersonal)
              AND (:estadoActivo IS NULL OR p.estadoActivo = :estadoActivo)
            """)
    List<Personal> buscarConFiltros(
            @Param("nombre") String nombre,
            @Param("tipoPersonal") TipoPersonal tipoPersonal,
            @Param("estadoActivo") Boolean estadoActivo);
}
