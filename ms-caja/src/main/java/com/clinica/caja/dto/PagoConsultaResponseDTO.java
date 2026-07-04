package com.clinica.caja.dto;

import com.clinica.caja.model.EstadoPagoConsulta;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Estado del pago de consulta")
public class PagoConsultaResponseDTO {

    @Schema(description = "ID del pago", example = "10")
    private Long id;

    @Schema(description = "ID de la cita asociada", example = "100")
    private Long idCita;

    @Schema(description = "ID del paciente", example = "42")
    private Long idPaciente;

    @Schema(description = "Monto calculado según tarifa de especialidad", example = "80.00")
    private BigDecimal monto;

    @Schema(description = "Estado del pago", example = "PENDIENTE")
    private EstadoPagoConsulta estado;

    @Schema(description = "Crédito de NC aplicado", example = "80.00")
    private BigDecimal montoCreditoAplicado;

    @Schema(description = "Monto real a cobrar (monto - montoCreditoAplicado)", example = "0.00")
    private BigDecimal montoACobrar;
}
