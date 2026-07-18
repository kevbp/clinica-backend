package com.clinica.caja.client;

import com.clinica.caja.client.dto.PacienteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-pacientes")
public interface PacientesFeignClient {

    @GetMapping("/pacientes/{id}")
    ResponseEntity<PacienteDTO> obtenerPaciente(@PathVariable Long id);
}
