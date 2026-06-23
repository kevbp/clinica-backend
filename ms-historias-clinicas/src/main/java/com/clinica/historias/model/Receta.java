package com.clinica.historias.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "recetas")
@Getter
@Setter
@NoArgsConstructor
public class Receta {

    @Id
    private String idReceta;

    @Indexed
    private String idEpisodioClinico;

    // Denormalizado para permitir GET /recetas/paciente/{idPaciente} sin escanear episodios
    @Indexed
    private Long idPaciente;

    private Long idPersonalMedico;
    private List<LineaReceta> lineas;
}
