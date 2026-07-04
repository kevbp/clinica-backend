package com.clinica.atencion.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// POJO transitorio — solo existe en Redis, nunca en BD persistente.
// Clave Redis: atencion:borrador:{idCita}  TTL: 8 horas
@Data
@NoArgsConstructor
public class BorradorAtencion {

    private Long idCita;
    private Long idPaciente;
    private Long idPersonalMedico;

    // S — Subjetivo
    private String motivoConsulta;

    // O — Objetivo
    private SignosVitalesBorrador signosVitales;

    // A — Evaluación
    private DiagnosticoBorrador diagnostico;
    private String observacionesClinicas;

    // P — Plan
    private List<LineaRecetaBorrador> lineasReceta = new ArrayList<>();
    private List<LineaOrdenBorrador>  lineasOrden  = new ArrayList<>();
}
