package com.clinica.historias.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/** Episodio clínico que no pudo persistirse tras 3 reintentos — guardado en DLQ para trazabilidad. */
@Document(collection = "episodios_fallidos")
@Getter @Setter @NoArgsConstructor
public class EpisodioFallido {

    @Id
    private String id;

    private String correlationId;
    private String payload;
    private String errorMensaje;
    private LocalDateTime fechaFallo;
    private Integer reintentos;
}
