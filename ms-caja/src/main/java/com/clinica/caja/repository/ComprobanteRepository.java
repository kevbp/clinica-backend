package com.clinica.caja.repository;

import com.clinica.caja.model.Comprobante;
import com.clinica.caja.model.TipoComprobante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
    Optional<Comprobante> findByTipoAndIdOrigen(TipoComprobante tipo, Long idOrigen);
    List<Comprobante> findByTipoAndIdOrigenIn(TipoComprobante tipo, List<Long> idsOrigen);
}
