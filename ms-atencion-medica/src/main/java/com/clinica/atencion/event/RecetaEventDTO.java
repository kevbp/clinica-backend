package com.clinica.atencion.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RecetaEventDTO {
    private List<LineaRecetaEventDTO> lineas;
}
