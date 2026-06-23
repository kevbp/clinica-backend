package com.clinica.atencion.client;

import com.clinica.atencion.client.dto.AntecedenteClinicoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "ms-pacientes")
public interface PacientesFeignClient {

    @GetMapping("/pacientes/{id}/antecedentes")
    ResponseEntity<List<AntecedenteClinicoDTO>> obtenerAntecedentes(@PathVariable Long id);
}
