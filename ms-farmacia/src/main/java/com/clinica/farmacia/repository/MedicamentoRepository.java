package com.clinica.farmacia.repository;

import com.clinica.farmacia.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    List<Medicamento> findByNombreContainingIgnoreCaseOrPrincipioActivoContainingIgnoreCase(
            String nombre, String principioActivo);
}
