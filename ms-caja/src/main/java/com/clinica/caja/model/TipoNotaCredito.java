package com.clinica.caja.model;

public enum TipoNotaCredito {
    /** Cancelación con ≥24h de anticipación → devolución 100 %. */
    CANCELACION_ANTICIPADA,
    /** Cancelación con <24h de anticipación → devolución 70 %, penalidad 30 %. */
    CANCELACION_TARDIA,
    /** La clínica cancela por fuerza mayor → devolución 100 %. */
    CANCELACION_POR_CLINICA,
    /** No-show (auto-cancelación por retraso >15 min) → devolución 70 %, penalidad 30 %. */
    NO_SHOW,
    /** Error de cobro o pago duplicado → devolución 100 %. */
    ERROR_COBRO
}
