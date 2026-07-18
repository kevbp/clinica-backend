package com.clinica.historias.repository;

import com.clinica.historias.model.EpisodioFallido;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EpisodioFallidoRepository extends MongoRepository<EpisodioFallido, String> {
}
