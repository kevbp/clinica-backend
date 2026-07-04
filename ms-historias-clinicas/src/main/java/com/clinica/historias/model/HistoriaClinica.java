package com.clinica.historias.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "historias_clinicas")
@Getter
@Setter
@NoArgsConstructor
public class HistoriaClinica {

    @Id
    private String id;

    // Código único legible: HC-00000001 (basado en idPaciente con padding de 8 dígitos)
    @Indexed(unique = true)
    private String codigoHistoria;

    @Indexed(unique = true)
    private Long idPaciente;

    private LocalDateTime fechaCreacion;
    private String estado; // ACTIVA / INACTIVA
}
