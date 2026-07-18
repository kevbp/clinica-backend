package com.clinica.horarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Resultado de la creación de turnos en lote")
public class ProgramacionHorarioBatchResponseDTO {

    @Schema(description = "Total de fechas enviadas en la solicitud")
    private int total;

    @Schema(description = "Total de turnos creados exitosamente")
    private int creados;

    @Schema(description = "Turnos creados")
    private List<ProgramacionHorarioResponseDTO> turnos;
}
