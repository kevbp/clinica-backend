package com.clinica.atencion.client;

import com.clinica.atencion.client.dto.ExamenCatalogoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-laboratorio")
public interface LaboratorioFeignClient {

    // Solo catálogo — nunca precio, nunca crea ExamenAutorizado
    @GetMapping("/examenes/{id}")
    ResponseEntity<ExamenCatalogoDTO> obtenerExamen(@PathVariable Long id);
}
