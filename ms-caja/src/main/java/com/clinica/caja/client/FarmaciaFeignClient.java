package com.clinica.caja.client;

import com.clinica.caja.client.dto.DescontarStockRequestDTO;
import com.clinica.caja.client.dto.DescontarStockResponseDTO;
import com.clinica.caja.client.dto.PrecioMedicamentoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-farmacia")
public interface FarmaciaFeignClient {

    @GetMapping("/medicamentos/{id}/precio")
    ResponseEntity<PrecioMedicamentoDTO> obtenerPrecio(@PathVariable Long id);

    @PatchMapping("/medicamentos/{id}/descontar-stock")
    ResponseEntity<DescontarStockResponseDTO> descontarStock(@PathVariable Long id,
                                                              @RequestBody DescontarStockRequestDTO request);
}
