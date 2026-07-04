package com.clinica.historias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Cabecera de la historia clínica de un paciente")
public class HistoriaClinicaResponseDTO {

    @Schema(description = "ID interno (ObjectId MongoDB)")
    private String id;

    @Schema(description = "Código único de la historia clínica", example = "HC-00000004")
    private String codigoHistoria;

    @Schema(description = "ID del paciente propietario de esta historia")
    private Long idPaciente;

    @Schema(description = "Fecha de creación de la historia (primera atención)")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Estado: ACTIVA / INACTIVA")
    private String estado;
}
