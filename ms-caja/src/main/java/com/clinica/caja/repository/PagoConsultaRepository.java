package com.clinica.caja.repository;

import com.clinica.caja.model.PagoConsulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoConsultaRepository extends JpaRepository<PagoConsulta, Long> {

    Optional<PagoConsulta> findByIdCita(Long idCita);

    List<PagoConsulta> findByIdPaciente(Long idPaciente);
}
