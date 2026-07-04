package com.clinica.atencion.catalog.repository;

import com.clinica.atencion.catalog.Cie10;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface Cie10Repository extends JpaRepository<Cie10, String> {

    @Query("""
        SELECT c FROM Cie10 c
        WHERE LOWER(c.codigo) LIKE LOWER(CONCAT(:q, '%'))
           OR LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :q, '%'))
        ORDER BY c.codigo
        LIMIT 30
        """)
    List<Cie10> buscar(@Param("q") String q);
}
