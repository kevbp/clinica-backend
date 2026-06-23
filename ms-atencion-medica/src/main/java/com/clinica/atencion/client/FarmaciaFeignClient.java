package com.clinica.atencion.client;

import com.clinica.atencion.client.dto.DisponibilidadDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-farmacia")
public interface FarmaciaFeignClient {

    // Solo lectura — advertencia al médico. Nunca descuenta stock ni consulta precio.
    @GetMapping("/medicamentos/{id}/disponibilidad")
    ResponseEntity<DisponibilidadDTO> obtenerDisponibilidad(@PathVariable Long id);
}
