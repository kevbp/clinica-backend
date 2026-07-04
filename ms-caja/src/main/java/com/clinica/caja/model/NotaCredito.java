package com.clinica.caja.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "notas_credito")
@Getter @Setter @NoArgsConstructor
public class NotaCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_paciente", nullable = false)
    private Long idPaciente;

    /** Monto devuelto al paciente (70 % o 100 % según tipo). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    /** Monto retenido como penalidad (30 % en CANCELACION_TARDIA / NO_SHOW; 0 en los demás). */
    @Column(name = "monto_retenido", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoRetenido;

    @Column(name = "id_pago_consulta_origen", nullable = false)
    private Long idPagoConsultaOrigen;

    @Column(nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotaCredito tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoNotaCredito estado;

    /** Número formal correlativo (ej. NC-20260703-00042) para trazabilidad SUNAT. */
    @Column(unique = true)
    private String numero;

    /** ID del comprobante que esta NC modifica (referencia al comprobante original). */
    @Column(name = "id_comprobante_relacionado")
    private Long idComprobanteRelacionado;
}
