package com.clinica.historias.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "ordenes_laboratorio")
@Getter
@Setter
@NoArgsConstructor
public class OrdenLaboratorio {

    @Id
    private String idOrden;

    @Indexed
    private String idEpisodioClinico;

    // Denormalizado para GET /ordenes/paciente/{idPaciente}
    @Indexed
    private Long idPaciente;

    private Long idPersonalMedico;

    private LocalDateTime fechaEmision;

    // Snapshots embebidos para trazabilidad sin referencias débiles
    private PacienteSnapshot paciente;
    private MedicoSnapshot   medico;

    private List<LineaOrden> lineas;
}
