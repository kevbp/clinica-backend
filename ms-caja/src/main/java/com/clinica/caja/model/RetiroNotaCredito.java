package com.clinica.caja.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "retiros_nota_credito")
@Getter @Setter @NoArgsConstructor
public class RetiroNotaCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** La NC que se está retirando — se marca USADA al crear el retiro. */
    @Column(name = "id_nota_credito", nullable = false, unique = true)
    private Long idNotaCredito;

    @Column(name = "id_paciente", nullable = false)
    private Long idPaciente;

    /** Monto de la NC (readonly — se copia de la NC para trazabilidad). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "nombre_banco", nullable = false)
    private String nombreBanco;

    @Column(name = "numero_cuenta", nullable = false)
    private String numeroCuenta;

    @Column(name = "nombre_titular", nullable = false)
    private String nombreTitular;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRetiro estado;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "correo_paciente")
    private String correoPaciente;
}
