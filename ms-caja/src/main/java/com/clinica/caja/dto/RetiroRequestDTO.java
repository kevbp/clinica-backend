package com.clinica.caja.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Datos bancarios para solicitar el retiro de una nota de crédito")
public class RetiroRequestDTO {

    @NotNull(message = "El ID de la nota de crédito es obligatorio")
    @Schema(description = "ID de la nota de crédito a retirar", example = "3")
    private Long idNotaCredito;

    @NotBlank(message = "El nombre del banco es obligatorio")
    @Schema(description = "Nombre del banco", example = "BCP")
    private String nombreBanco;

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Schema(description = "Número de cuenta bancaria", example = "191-12345678-0-62")
    private String numeroCuenta;

    @NotBlank(message = "El nombre del titular es obligatorio")
    @Schema(description = "Nombre completo del titular de la cuenta", example = "María Elena Torres Vásquez")
    private String nombreTitular;

    @Schema(description = "Correo para recibir confirmación del retiro")
    private String correoConfirmacion;
}
