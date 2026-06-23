package com.clinica.historias.repository;

import com.clinica.historias.model.AdendaClinica;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AdendaClinicoRepository extends MongoRepository<AdendaClinica, String> {

    List<AdendaClinica> findByIdEpisodioPadreOrderByFechaCorreccionAsc(String idEpisodioPadre);
}
