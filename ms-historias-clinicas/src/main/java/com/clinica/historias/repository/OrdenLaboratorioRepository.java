package com.clinica.historias.repository;

import com.clinica.historias.model.OrdenLaboratorio;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OrdenLaboratorioRepository extends MongoRepository<OrdenLaboratorio, String> {

    List<OrdenLaboratorio> findByIdPaciente(Long idPaciente);

    Optional<OrdenLaboratorio> findByIdEpisodioClinico(String idEpisodioClinico);
}
