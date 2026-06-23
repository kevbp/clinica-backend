package com.clinica.personal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "personal")
@Getter
@Setter
@NoArgsConstructor
public class Personal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(name = "documento_identidad", nullable = false, unique = true)
    private String documentoIdentidad;

    private String contacto;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "estado_activo", nullable = false)
    private Boolean estadoActivo = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_personal", nullable = false)
    private TipoPersonal tipoPersonal;

    @Column(name = "keycloak_user_id", unique = true)
    private String keycloakUserId;
}
