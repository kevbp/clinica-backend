package com.clinica.historias.repository;

import com.clinica.historias.model.Receta;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RecetaRepository extends MongoRepository<Receta, String> {

    List<Receta> findByIdPaciente(Long idPaciente);

    Optional<Receta> findByIdEpisodioClinico(String idEpisodioClinico);
}
