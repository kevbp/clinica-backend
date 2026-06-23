package com.clinica.caja.repository;

import com.clinica.caja.model.Proforma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProformaRepository extends JpaRepository<Proforma, Long> {

    List<Proforma> findByIdPaciente(Long idPaciente);
}
