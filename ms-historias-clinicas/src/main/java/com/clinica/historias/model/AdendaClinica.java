package com.clinica.historias.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "adendas_clinicas")
@Getter
@Setter
@NoArgsConstructor
public class AdendaClinica {

    @Id
    private String idAdenda;

    @Indexed
    private String idEpisodioPadre;

    private LocalDateTime fechaCorreccion;
    private Long idPersonalMedico;
    private String textoRectificacion;
}
