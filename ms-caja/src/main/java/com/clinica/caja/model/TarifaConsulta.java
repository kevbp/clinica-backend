package com.clinica.caja.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tarifas_consulta")
@Getter @Setter @NoArgsConstructor
public class TarifaConsulta {

    // PK es el idEspecialidad de ms-personal (referencia débil, una tarifa por especialidad)
    @Id
    @Column(name = "id_especialidad")
    private Long idEspecialidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;
}
