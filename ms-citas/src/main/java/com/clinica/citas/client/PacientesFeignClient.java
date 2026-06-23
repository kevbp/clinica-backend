package com.clinica.citas.client;

import com.clinica.citas.client.dto.PacienteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-pacientes")
public interface PacientesFeignClient {

    // Retorna el perfil completo; si el paciente no existe lanza FeignException 404.
    // Usamos este endpoint en lugar de /existe para obtener también el contacto (correo).
    @GetMapping("/pacientes/{id}")
    ResponseEntity<PacienteDTO> obtenerPaciente(@PathVariable Long id);
}
