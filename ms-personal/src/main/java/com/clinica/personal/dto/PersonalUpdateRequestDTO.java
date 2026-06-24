package com.clinica.personal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Campos actualizables del personal. Solo se modifican los campos no nulos.")
public class PersonalUpdateRequestDTO {

    @Schema(description = "Nombres del personal", example = "Carlos Alberto")
    private String nombres;

    @Schema(description = "Apellidos del personal", example = "Ramírez Soto")
    private String apellidos;

    @Schema(description = "Número de documento de identidad", example = "12345678")
    private String documentoIdentidad;

    @Schema(description = "Teléfono o email de contacto", example = "987654321")
    private String contacto;

    @Schema(description = "Fecha de ingreso a la clínica", example = "2023-03-15")
    private LocalDate fechaIngreso;
}
