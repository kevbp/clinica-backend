package com.clinica.historias.event.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrdenEventDTO {
    private List<LineaOrdenEventDTO> lineas;
}
