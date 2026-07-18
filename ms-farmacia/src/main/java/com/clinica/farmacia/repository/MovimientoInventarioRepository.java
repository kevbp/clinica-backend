package com.clinica.farmacia.repository;

import com.clinica.farmacia.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByIdMedicamentoOrderByFechaDesc(Long idMedicamento);

    List<MovimientoInventario> findByIdMedicamentoAndFechaBetweenOrderByFechaDesc(
            Long idMedicamento, LocalDateTime desde, LocalDateTime hasta);
}
