package com.clinica.caja.repository;

import com.clinica.caja.model.RetiroNotaCredito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RetiroNotaCreditoRepository extends JpaRepository<RetiroNotaCredito, Long> {
    List<RetiroNotaCredito> findByIdPaciente(Long idPaciente);

    boolean existsByIdNotaCredito(Long idNotaCredito);
}
