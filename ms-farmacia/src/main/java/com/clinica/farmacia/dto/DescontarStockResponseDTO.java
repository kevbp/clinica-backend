package com.clinica.farmacia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Resultado del descuento de stock. exitoso=false indica stock insuficiente " +
        "(fallo controlado: ms-caja marca el ítem como NO_DISPONIBLE sin abortar la transacción).")
public class DescontarStockResponseDTO {

    @Schema(description = "true si el descuento se realizó, false si no había stock suficiente",
            example = "true")
    private boolean exitoso;

    @Schema(description = "Cantidad solicitada", example = "2")
    private Integer cantidadSolicitada;

    @Schema(description = "Cantidad efectivamente descontada (0 si exitoso=false)", example = "2")
    private Integer cantidadDescontada;

    @Schema(description = "Mensaje descriptivo", example = "Stock descontado correctamente aplicando FEFO")
    private String mensaje;

    public static DescontarStockResponseDTO exito(int cantidad) {
        DescontarStockResponseDTO r = new DescontarStockResponseDTO();
        r.exitoso = true;
        r.cantidadSolicitada = cantidad;
        r.cantidadDescontada = cantidad;
        r.mensaje = "Stock descontado correctamente aplicando FEFO";
        return r;
    }

    public static DescontarStockResponseDTO fallo(int solicitado, int disponible) {
        DescontarStockResponseDTO r = new DescontarStockResponseDTO();
        r.exitoso = false;
        r.cantidadSolicitada = solicitado;
        r.cantidadDescontada = 0;
        r.mensaje = "Stock insuficiente. Disponible: " + disponible + ", requerido: " + solicitado;
        return r;
    }
}
