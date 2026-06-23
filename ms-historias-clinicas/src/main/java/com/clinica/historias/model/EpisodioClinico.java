package com.clinica.historias.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

// Inmutable: solo se inserta (MongoTemplate.insert). El repositorio no expone save/update.
@Document(collection = "episodios_clinicos")
@Getter
@Setter
@NoArgsConstructor
public class EpisodioClinico {

    @Id
    private String idEpisodio;

    private Long idPaciente;
    private Long idCita;
    private Long idPersonalMedico;
    private LocalDateTime fechaAtencion;
    private Diagnostico diagnostico;
    private String observacionesClinicas;
}
