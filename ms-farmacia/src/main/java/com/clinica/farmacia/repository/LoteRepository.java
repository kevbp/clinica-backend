package com.clinica.farmacia.repository;

import com.clinica.farmacia.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findByMedicamentoId(Long idMedicamento);
}
