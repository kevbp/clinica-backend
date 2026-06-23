package com.clinica.caja.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Datos para crear el cobro pendiente de una consulta recién agendada")
public class PagoConsultaRequestDTO {

    @NotNull
    @Schema(description = "ID de la cita PENDIENTE_PAGO", example = "100",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idCita;

    @NotNull
    @Schema(description = "ID del paciente", example = "42",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idPaciente;

    @NotNull
    @Schema(description = "ID del médico (para calcular tarifa según especialidad)", example = "5",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idPersonalMedico;

    @Schema(description = "Correo o contacto del paciente — se incluirá en el evento PagoConsultaConfirmado " +
                          "para que ms-notificaciones pueda enviar el correo sin necesitar Feign propio",
            example = "paciente@email.com")
    private String correoPaciente;
}
