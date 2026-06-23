package com.clinica.historias.event.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecetaEventDTO {
    private List<LineaRecetaEventDTO> lineas;
}
