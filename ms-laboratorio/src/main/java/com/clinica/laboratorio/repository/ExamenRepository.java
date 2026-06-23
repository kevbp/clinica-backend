package com.clinica.laboratorio.repository;

import com.clinica.laboratorio.model.Examen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamenRepository extends JpaRepository<Examen, Long> {
}
