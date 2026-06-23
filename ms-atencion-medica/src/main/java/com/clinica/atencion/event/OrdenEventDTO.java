package com.clinica.atencion.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrdenEventDTO {
    private List<LineaOrdenEventDTO> lineas;
}
