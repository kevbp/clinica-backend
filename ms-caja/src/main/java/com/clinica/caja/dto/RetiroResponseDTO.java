package com.clinica.caja.dto;

import com.clinica.caja.model.EstadoRetiro;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Solicitud de retiro bancario de una nota de crédito")
public class RetiroResponseDTO {

    @Schema(description = "ID del retiro", example = "1")
    private Long id;

    @Schema(description = "ID de la nota de crédito retirada", example = "3")
    private Long idNotaCredito;

    @Schema(description = "ID del paciente", example = "42")
    private Long idPaciente;

    @Schema(description = "Monto a transferir", example = "80.00")
    private BigDecimal monto;

    @Schema(description = "Nombre del banco destino", example = "BCP")
    private String nombreBanco;

    @Schema(description = "Número de cuenta destino", example = "191-12345678-0-62")
    private String numeroCuenta;

    @Schema(description = "Nombre del titular de la cuenta", example = "María Elena Torres Vásquez")
    private String nombreTitular;

    @Schema(description = "Estado del retiro", example = "SOLICITADO")
    private EstadoRetiro estado;

    @Schema(description = "Fecha y hora de la solicitud")
    private LocalDateTime fechaSolicitud;
}
