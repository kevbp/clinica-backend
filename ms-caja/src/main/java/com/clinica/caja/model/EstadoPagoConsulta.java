package com.clinica.caja.model;

public enum EstadoPagoConsulta {
    PENDIENTE,
    PAGADO,
    PAGADO_SIN_CONFIRMAR  // estado de compensación de la Saga 14.1
}
