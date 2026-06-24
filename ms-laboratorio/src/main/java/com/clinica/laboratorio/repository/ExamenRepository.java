package com.clinica.laboratorio.repository;

import com.clinica.laboratorio.model.Examen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamenRepository extends JpaRepository<Examen, Long> {

    List<Examen> findByNombreContainingIgnoreCaseOrCategoriaContainingIgnoreCase(
            String nombre, String categoria);
}
