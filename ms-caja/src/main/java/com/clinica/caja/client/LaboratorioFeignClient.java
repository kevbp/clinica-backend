package com.clinica.caja.client;

import com.clinica.caja.client.dto.ExamenAutorizadoClientDTO;
import com.clinica.caja.client.dto.ExamenAutorizadoRequestDTO;
import com.clinica.caja.client.dto.PrecioExamenDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-laboratorio")
public interface LaboratorioFeignClient {

    @GetMapping("/examenes/{id}/precio")
    ResponseEntity<PrecioExamenDTO> obtenerPrecio(@PathVariable Long id);

    @PostMapping("/examenes-autorizados")
    ResponseEntity<ExamenAutorizadoClientDTO> autorizarExamen(@RequestBody ExamenAutorizadoRequestDTO request);
}
