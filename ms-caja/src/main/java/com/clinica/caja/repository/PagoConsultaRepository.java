package com.clinica.caja.repository;

import com.clinica.caja.model.PagoConsulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoConsultaRepository extends JpaRepository<PagoConsulta, Long> {

    Optional<PagoConsulta> findByIdCita(Long idCita);
}
