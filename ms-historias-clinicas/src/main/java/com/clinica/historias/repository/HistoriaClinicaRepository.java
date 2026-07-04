package com.clinica.historias.repository;

import com.clinica.historias.model.HistoriaClinica;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface HistoriaClinicaRepository extends MongoRepository<HistoriaClinica, String> {

    Optional<HistoriaClinica> findByIdPaciente(Long idPaciente);
}
