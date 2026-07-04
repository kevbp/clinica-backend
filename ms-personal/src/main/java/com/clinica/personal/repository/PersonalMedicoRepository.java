package com.clinica.personal.repository;

import com.clinica.personal.model.PersonalMedico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalMedicoRepository extends JpaRepository<PersonalMedico, Long> {

    Optional<PersonalMedico> findByPersonalId(Long personalId);

    boolean existsByEspecialidadId(Long especialidadId);
}
