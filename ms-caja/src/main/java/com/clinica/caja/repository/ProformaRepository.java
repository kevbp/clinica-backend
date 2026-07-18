package com.clinica.caja.repository;

import com.clinica.caja.model.Proforma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProformaRepository extends JpaRepository<Proforma, Long> {

    List<Proforma> findByIdPaciente(Long idPaciente);

    List<Proforma> findByIdReceta(String idReceta);

    List<Proforma> findByIdOrden(String idOrden);

    Optional<Proforma> findTopByIdRecetaOrderByFechaGeneracionDesc(String idReceta);

    Optional<Proforma> findTopByIdOrdenOrderByFechaGeneracionDesc(String idOrden);
}
