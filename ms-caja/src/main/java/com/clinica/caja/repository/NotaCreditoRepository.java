package com.clinica.caja.repository;

import com.clinica.caja.model.NotaCredito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotaCreditoRepository extends JpaRepository<NotaCredito, Long> {

    List<NotaCredito> findByIdPaciente(Long idPaciente);

    List<NotaCredito> findByIdPacienteAndEstado(Long idPaciente, com.clinica.caja.model.EstadoNotaCredito estado);
}
