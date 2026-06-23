package com.clinica.historias.repository;

import com.clinica.historias.model.EpisodioClinico;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

// Extiende Repository base (no MongoRepository) para NO exponer save/update/delete.
// La inserción se realiza exclusivamente via MongoTemplate en el servicio,
// garantizando la inmutabilidad del EHR a nivel de código.
public interface EpisodioClinicoRepository extends Repository<EpisodioClinico, String> {

    Optional<EpisodioClinico> findById(String id);

    List<EpisodioClinico> findByIdPaciente(Long idPaciente);
}
