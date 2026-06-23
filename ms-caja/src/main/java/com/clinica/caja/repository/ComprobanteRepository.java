package com.clinica.caja.repository;

import com.clinica.caja.model.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
}
