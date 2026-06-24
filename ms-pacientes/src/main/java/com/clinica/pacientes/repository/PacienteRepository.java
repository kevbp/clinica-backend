package com.clinica.pacientes.repository;

import com.clinica.pacientes.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    @Query("""
            SELECT p FROM Paciente p
            WHERE LOWER(CONCAT(p.nombres, ' ', p.apellidos)) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.documentoIdentidad) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    List<Paciente> buscar(@Param("q") String q);
}
