package com.clinica.caja.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "pagos_consulta")
@Getter @Setter @NoArgsConstructor
public class PagoConsulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_cita", nullable = false, unique = true)
    private Long idCita;

    @Column(name = "id_paciente", nullable = false)
    private Long idPaciente;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPagoConsulta estado;

    @Column(name = "correo_paciente")
    private String correoPaciente;

    @Column(name = "nombre_paciente")
    private String nombrePaciente;

    @Column(name = "especialidad")
    private String especialidad;

    @Column(name = "monto_credito_aplicado", precision = 10, scale = 2)
    private BigDecimal montoCreditoAplicado;
}
